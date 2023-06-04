package net.k1ra.orderfulfill.providers.ebay.api

import android.content.Context
import com.gap.hoodies_network.core.Failure
import com.gap.hoodies_network.core.HoodiesNetworkClient
import com.gap.hoodies_network.core.HoodiesNetworkError
import com.gap.hoodies_network.core.Result
import com.gap.hoodies_network.core.Success
import com.gap.hoodies_network.interceptor.Interceptor
import com.gap.hoodies_network.request.RetryableCancellableMutableRequest
import com.google.gson.Gson
import net.k1ra.orderfulfill.BuildConfig
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.providers.ebay.model.*
import net.k1ra.orderfulfill.secure_storage.db.PlatformApiAuthData
import net.k1ra.orderfulfill.utils.Constants
import java.util.*


class ApiClient(tokenDataStr: String, context: Context) {
    private val tokenData: TokenData = Gson().fromJson(tokenDataStr, TokenData::class.java)

    private val refreshTokenInterceptor = object: Interceptor(context) {
        override suspend fun interceptError(
            error: HoodiesNetworkError,
            retryableCancellableMutableRequest: RetryableCancellableMutableRequest,
            autoRetryAttempts: Int
        ) {
            //If error is an access token error, do refresh token and try again
            val errors = Gson().fromJson(error.message, Errors::class.java)
            if (error.message?.isNotBlank() == true && errors.errors.any { it.errorId == 1001 }) {
                val refreshHttpClient = HoodiesNetworkClient.Builder()
                    .baseUrl("https://api.${BuildConfig.EBAY_BASE_URL}/")
                    .build()

                val headers = hashMapOf("Authorization" to "Basic ${Base64.getEncoder().encodeToString("${BuildConfig.EBAY_CLIENT_ID}:${BuildConfig.EBAY_CLIENT_SECRET}".toByteArray())}")
                val body = hashMapOf(
                    "grant_type" to "refresh_token",
                    "refresh_token" to tokenData.refreshToken
                )

                val result = refreshHttpClient.postUrlQueryParamEncoded<RefreshTokenResponse>(
                    body,
                    "identity/v1/oauth2/token",
                    headers
                )

                //If result is success, save new access token and retry the original request. Otherwise, do nothing and let the original request fail through
                if (result is Success) {
                    tokenData.accessToken = result.value.accessToken
                    PlatformApiAuthData.store(Platform.EBAY, Gson().toJson(tokenData), context)

                    //Apply new Authorization header
                    val newHeaders = retryableCancellableMutableRequest.request.getHeaders() as MutableMap
                    newHeaders["Authorization"] = "Bearer ${tokenData.accessToken}"
                    retryableCancellableMutableRequest.request.setRequestHeaders(newHeaders)

                    retryableCancellableMutableRequest.retryRequest()
                }
            }
        }
    }

    private val httpClient = HoodiesNetworkClient.Builder()
        .baseUrl("https://api.${BuildConfig.EBAY_BASE_URL}/")
        .addHeader("Authorization", "Bearer ${tokenData.accessToken}")
        .addInterceptor(refreshTokenInterceptor)
        .build()

    suspend fun getPendingOrders() : Result<OrderSearchPagedCollection, HoodiesNetworkError> {
        return httpClient.getUrlQueryParamEncoded(
            hashMapOf(
                "filter" to Constants.eBayPendingOrderFilter
            ),
            "sell/fulfillment/v1/order"
        )
    }

    suspend fun getItemImage(item: LineItem) : String {
        val body = mutableMapOf( "legacy_item_id" to item.legacyItemId)
        if (item.legacyVariationId != null)
            body["legacy_variation_id"] = item.legacyVariationId

        val result =  httpClient.getUrlQueryParamEncoded<ListedItem>(
            body as HashMap<String, String>,
            "buy/browse/v1/item/get_item_by_legacy_id"
        )

        //We don't need complicated error handling logic here, it's just a product image. It's OK if it's missing
        return when(result) {
            is Success -> {
                result.value.image.imageUrl
            }
            is Failure -> ""
        }
    }

    suspend fun fulfillOrder(trackingNumber: String?, carrier: ShippingPlatformActions, order: PendingOrders.PendingOrder) : Boolean {
        val fulfilledItems = order.products.map { FulfilledLineItem(it.lineItemId, it.quantity) }

        val fulfillment: OrderFulfillment = if (trackingNumber != null) {
            OrderFulfillmentWithTracking(fulfilledItems, carrier.code, trackingNumber)
        } else {
            OrderFulfillment(fulfilledItems)
        }

        val result =  httpClient.post<OrderFulfillment, String>(
            "sell/fulfillment/v1/order/${order.id}/shipping_fulfillment",
            fulfillment,
        )

        return result is Success
    }
}