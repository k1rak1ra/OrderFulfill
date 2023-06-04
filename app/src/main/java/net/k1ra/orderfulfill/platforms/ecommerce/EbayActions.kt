package net.k1ra.orderfulfill.platforms.ecommerce

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.gap.hoodies_network.core.Failure
import com.gap.hoodies_network.core.HoodiesNetworkClient
import com.gap.hoodies_network.core.Success
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.orderfulfill.BuildConfig
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.feature.orders.model.OrderPaymentState
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.feature.orders.model.Product
import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.feature.orders.ui.PlatformPendingOrderFragment
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.providers.ebay.api.ApiClient
import net.k1ra.orderfulfill.providers.ebay.model.Errors
import net.k1ra.orderfulfill.secure_storage.CryptographyHelper
import net.k1ra.orderfulfill.utils.Constants
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Consumer
import java.util.HashMap
import java.util.stream.Collectors

class EbayActions : EcomPlatformActions {
    override val type = Platform.EBAY

    override fun getPlatformImage(context: Context): Bitmap {
        return AppCompatResources.getDrawable(context, R.drawable.ebay)!!.toBitmap()
    }

    //We use Oauth, so we set it up here
    override val authorizationType = PlatformAuthorizationType.OAUTH
    override val oAuthCSRFstate: String =
        Base64.getEncoder().encodeToString(CryptographyHelper.genIV())
    override val oAuthUrl = "https://auth.${BuildConfig.EBAY_BASE_URL}/oauth2/authorize" +
            "?client_id=${BuildConfig.EBAY_CLIENT_ID}" +
            "&redirect_uri=${BuildConfig.EBAY_RU_NAME}" +
            "&response_type=code" +
            "&scope=${Constants.ebayOAuthScopes}" +
            "&state=$oAuthCSRFstate"
    override val oAuthReturnUrl = "https://k1ra.net/ebay/accepted"

    /**
     * Code to verify oAuth request via "state" and then exchange the code for an access token
     */
    override val oAuthAccessTokenAcquisitionRunnable =
        Consumer<DataWorker<HashMap<String, String>, String>> {
            CoroutineScope(Dispatchers.IO).launch {
                //I don't know why it's complaining about about an inappropriate blocking call here, we're on an IO coroutine...
                if (it.data["state"] == URLEncoder.encode(
                        oAuthCSRFstate,
                        StandardCharsets.US_ASCII.toString()
                    )
                ) {
                    val client = HoodiesNetworkClient.Builder()
                        .baseUrl("https://api.${BuildConfig.EBAY_BASE_URL}/")
                        .build()

                    val headers = hashMapOf(
                        "Authorization" to "Basic ${
                            Base64.getEncoder()
                                .encodeToString("${BuildConfig.EBAY_CLIENT_ID}:${BuildConfig.EBAY_CLIENT_SECRET}".toByteArray())
                        }"
                    )
                    val body = hashMapOf(
                        "grant_type" to "authorization_code",
                        "redirect_uri" to BuildConfig.EBAY_RU_NAME,
                        "code" to it.data["code"]!!
                    )

                    val result = client.postUrlQueryParam<String>(
                        body,
                        "identity/v1/oauth2/token",
                        headers
                    )

                    when (result) {
                        is Success -> it.callback.accept(
                            DataWorkerResult(
                                SuccessFail.SUCCESS,
                                result.value
                            )
                        )
                        is Failure -> it.callback.accept(
                            DataWorkerResult(
                                SuccessFail.FAILURE,
                                R.string.oauth_access_token_fail.toString()
                            )
                        )
                    }
                } else {
                    it.callback.accept(
                        DataWorkerResult(
                            SuccessFail.FAILURE,
                            R.string.oauth_forged_request.toString()
                        )
                    )
                }
            }
        }

    //OAuth is used, so no API key entry dialog
    override fun apiKeyEntryDialog(
        fragment: PlatformPendingOrderFragment,
        dw: DataWorker<Unit, String>
    ): Dialog? {
        return null
    }

    private var apiClient: ApiClient? = null

    private fun initApi(tokenDataStr: String, context: Context): ApiClient {
        if (apiClient == null)
            apiClient = ApiClient(tokenDataStr, context)
        return apiClient!!
    }

    override suspend fun getPendingOrders(apiAuthData: String, context: Context): PendingOrders {
        val api = initApi(apiAuthData, context)

        return when (val result = api.getPendingOrders()) {
            is Success -> {
                val orders = result.value.orders.map { order ->
                    val orderContents = order.lineItems.map { item ->
                        Product(
                            item.lineItemId,
                            "%.2f".format(item.lineItemCost.value),
                            item.title,
                            api.getItemImage(item),
                            item.quantity,
                            item.variationAspects?.stream()?.map { "${it.name} = ${it.value}\n" }?.collect(Collectors.joining()) ?: context.getString(R.string.standard)
                        )
                    }


                    PendingOrders.PendingOrder(
                        order.orderId,
                        DestinationAddress(
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.addressLine1,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.addressLine2 ?: "",
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.city,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.countryCode,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.postalCode,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.contactAddress.stateOrProvince,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.fullName,
                            order.fulfillmentStartInstructions.first().shippingStep.shipTo.primaryPhone.phoneNumber
                        ),
                        "%.2f".format(order.pricingSummary.deliveryCost.value),
                        OrderPaymentState.valueOf(order.orderPaymentStatus),
                        ZonedDateTime.parse(order.creationDate),
                        orderContents
                    )
                }

                PendingOrders.Success(orders)
            }
            is Failure -> {
                //De init api client so we start fresh next time
                apiClient = null
                val errors = Gson().fromJson(result.reason.message, Errors::class.java)
                if (errors.errors.any { it.errorId == 1001 }) {
                    PendingOrders.Failure(
                        RequestFailureType.AUTH_INVALID,
                        ""
                    ) //No message needed, user gets kicked out
                } else {
                    PendingOrders.Failure(
                        RequestFailureType.OTHER,
                        errors.errors.firstOrNull()?.longMessage
                            ?: context.getString(R.string.unknown_error)
                    )
                }
            }
        }
    }

    override suspend fun updateTracking(
        apiAuthData: String,
        context: Context,
        order: PendingOrders.PendingOrder,
        trackingNumber: String?,
        carrier: ShippingPlatformActions
    ): Boolean {
        return initApi(apiAuthData, context).fulfillOrder(trackingNumber, carrier, order)
    }
}