package net.k1ra.orderfulfill.providers.shopify.api

import com.gap.hoodies_network.core.*
import com.google.gson.Gson
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.providers.shopify.model.*

class ApiClient(apiAuthData: String?) {
    private val apiAuth = Gson().fromJson(apiAuthData, ShopifyApiAuth::class.java)

    private val httpClient = HoodiesNetworkClient.Builder()
        .baseUrl("https://${apiAuth.storeId}.myshopify.com/admin/api/2023-01/")
        .addHeader("X-Shopify-Access-Token", apiAuth.key)
        .build()

    suspend fun getPendingOrders() : Result<ShopifyOrderWrapper, HoodiesNetworkError> {
        return httpClient.get("orders.json?status=open")
    }

    suspend fun getProductImage(productId: String) : String {
        return when (val imageSet = httpClient.get<ImageWrapper>("products/$productId/images.json")) {
            is Success -> imageSet.value.images.firstOrNull()?.src ?: ""
            is Failure -> ""
        }
    }

    suspend fun updateTracking(order: PendingOrders.PendingOrder, trackingNumber: String?, carrier: ShippingPlatformActions) : Boolean {
        return when (val fulfillmentOrders = httpClient.get<FulfillmentOrderInfoWrapper>("orders/${order.id}/fulfillment_orders.json")){
            is Success -> {
                val fulfillmentId = fulfillmentOrders.value.fulfillmentOrders.first()

                val fulfillmentOrder = FulfillmentOrderWithLineItems(
                    fulfillmentId.id,
                    fulfillmentId.lineItems
                )
                val fulfillment: ShopifyFulfillment = if (trackingNumber == null) {
                    ShopifyFulfillment(
                        true,
                        listOf(fulfillmentOrder)
                    )
                } else {
                    ShopifyFulfillmentWithTracking(
                        true,
                        listOf(fulfillmentOrder),
                        ShopifyTrackingInfo(
                            trackingNumber,
                            carrier.getTrackingUrl(trackingNumber),
                            carrier.code
                        )
                    )
                }

                return when (httpClient.post<ShopifyFulfillmentWrapper, String>("fulfillments.json", ShopifyFulfillmentWrapper(fulfillment))) {
                    is Success -> true
                    is Failure -> false
                }
            }
            is Failure -> false
        }
    }
}