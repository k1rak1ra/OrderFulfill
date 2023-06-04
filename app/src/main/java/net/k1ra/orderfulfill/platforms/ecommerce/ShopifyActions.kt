package net.k1ra.orderfulfill.platforms.ecommerce

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import com.gap.hoodies_network.core.Failure
import com.gap.hoodies_network.core.HoodiesNetworkError
import com.gap.hoodies_network.core.Success
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.feature.orders.model.OrderPaymentState
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.feature.orders.model.Product
import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.feature.orders.ui.PlatformPendingOrderFragment
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.providers.shopify.api.ApiClient
import net.k1ra.orderfulfill.providers.shopify.model.ShopifyApiAuth
import java.time.ZonedDateTime
import java.util.function.Consumer

class ShopifyActions : EcomPlatformActions {
    override val type = Platform.SHOPIFY

    override fun getPlatformImage(context: Context): Bitmap {
        return AppCompatResources.getDrawable(context, R.drawable.shopify)!!.toBitmap()
    }

    override val authorizationType = PlatformAuthorizationType.API_KEY

    //This is blank since we're not using Oauth
    override val oAuthUrl = ""
    override val oAuthCSRFstate = ""
    override val oAuthReturnUrl = ""
    override val oAuthAccessTokenAcquisitionRunnable = Consumer<DataWorker<HashMap<String, String>, String>> {}

    //We use API key entry, so we set up our API key entry dialog here
    override fun apiKeyEntryDialog(fragment: PlatformPendingOrderFragment, dw: DataWorker<Unit, String>): Dialog? {
        //Prompt user for the API key via an AlertDialog with an EditText
        val builder = AlertDialog.Builder(fragment.requireContext())
        builder.setTitle(R.string.input_required)

        val apiKeyInput = EditText(fragment.requireContext())
        apiKeyInput.hint = fragment.requireContext().getString(R.string.give_api_key_shopify)
        apiKeyInput.inputType = InputType.TYPE_CLASS_TEXT

        val storeIdInput = EditText(fragment.requireContext())
        storeIdInput.hint = fragment.requireContext().getString(R.string.give_shop_id_shopify)
        storeIdInput.inputType = InputType.TYPE_CLASS_TEXT

        val lay = LinearLayout(fragment.requireContext())
        lay.orientation = LinearLayout.VERTICAL
        lay.addView(storeIdInput)
        lay.addView(apiKeyInput)

        builder.setView(lay)
        builder.setPositiveButton(R.string.cont) { dialog, _ ->
            dialog.cancel()

            //Send the API auth data string to the Consumer<String>, so the ViewModel can save the key and then re-init
            CoroutineScope(Dispatchers.IO).launch {
                dw.callback.accept(DataWorkerResult(SuccessFail.SUCCESS, Gson().toJson(ShopifyApiAuth(
                    storeIdInput.text.toString(),
                    apiKeyInput.text.toString()
                ))))
                fragment.viewModel.init(fragment.requireContext(), this@ShopifyActions)
            }
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            //Abort and go back if user clicks cancel
            dialog.cancel()
            fragment.childFragmentManager.popBackStackImmediate()
        }

        return builder.create()
    }

    fun returnFailureReason(error: HoodiesNetworkError) : PendingOrders.Failure {
        return if (error.code == 401 || error.code == 404)
            PendingOrders.Failure(RequestFailureType.AUTH_INVALID, error.message ?: "")
        else
            PendingOrders.Failure(RequestFailureType.OTHER, error.message ?: "")
    }

    override suspend fun getPendingOrders(apiAuthData: String, context: Context) : PendingOrders {
        val client = ApiClient(apiAuthData)

        return when (val orderResponse = client.getPendingOrders()) {
            is Success -> {
                val pendingOrders = orderResponse.value.orders.map { order ->
                    val products = order.lineItems.map { item ->
                        Product(
                            item.id,
                            item.priceSet.shopMoney.amount,
                            item.name,
                            client.getProductImage(item.productId),
                            item.quantity,
                            item.variantTitle ?: context.getString(R.string.standard)
                        )
                    }

                    PendingOrders.PendingOrder(
                        order.id,
                        DestinationAddress(
                            order.shippingAddress.address1,
                            order.shippingAddress.address2 ?: "",
                            order.shippingAddress.city,
                            order.shippingAddress.countryCode,
                            order.shippingAddress.zip,
                            order.shippingAddress.provinceCode ?: "",
                            order.shippingAddress.name,
                            order.shippingAddress.phone ?: ""
                        ),
                        order.totalShippingPriceSet.shopMoney.amount,
                        OrderPaymentState.PAID,
                        ZonedDateTime.parse(order.createdAt),
                        products
                    )
                }
                PendingOrders.Success(pendingOrders)
            }
            is Failure -> returnFailureReason(orderResponse.reason)
        }
    }

    override suspend fun updateTracking(
        apiAuthData: String,
        context: Context,
        order: PendingOrders.PendingOrder,
        trackingNumber: String?,
        carrier: ShippingPlatformActions
    ): Boolean {
        return ApiClient(apiAuthData).updateTracking(order, trackingNumber, carrier)
    }
}