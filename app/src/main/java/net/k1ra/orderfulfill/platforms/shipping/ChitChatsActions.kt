package net.k1ra.orderfulfill.platforms.shipping

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import com.gap.hoodies_network.core.Failure
import com.gap.hoodies_network.core.Success
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.gcardone.junidecode.Junidecode.unidecode
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.providers.chitchats.api.ApiClient
import net.k1ra.orderfulfill.providers.chitchats.model.ChitChatsApiAuth
import net.k1ra.orderfulfill.providers.chitchats.model.ChitChatsShipment
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentInfo
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate
import java.util.function.Consumer


class ChitChatsActions : ShippingPlatformActions {
    override val type = Platform.CHIT_CHATS
    override val requiresApiAuth = true
    override val code = "ChitChatsExpress"

    override fun apiKeyEntryDialog(context: Context, dw: DataWorker<Unit, String>): Dialog? {
        //Prompt user for the API key via an AlertDialog with an EditText
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.input_required)

        val apiKeyInput = EditText(context)
        apiKeyInput.hint = context.getString(R.string.give_api_key_chitchats)
        apiKeyInput.inputType = InputType.TYPE_CLASS_TEXT

        val shipperIdInput = EditText(context)
        shipperIdInput.hint = context.getString(R.string.give_shipper_id_chitchats)
        shipperIdInput.inputType = InputType.TYPE_CLASS_TEXT

        val lay = LinearLayout(context)
        lay.orientation = LinearLayout.VERTICAL
        lay.addView(shipperIdInput)
        lay.addView(apiKeyInput)

        builder.setView(lay)
        builder.setPositiveButton(R.string.cont) { dialog, _ ->
            dialog.cancel()

            CoroutineScope(Dispatchers.IO).launch {
                dw.callback.accept(DataWorkerResult(SuccessFail.SUCCESS, Gson().toJson(ChitChatsApiAuth(
                    shipperIdInput.text.toString(),
                    apiKeyInput.text.toString()
                ))))
            }
        }
        builder.setNegativeButton(R.string.cancel) { dialog, _ ->
            //Abort and go back if user clicks cancel
            dialog.cancel()
        }

        return builder.create()
    }

    override suspend fun getShippingOptions(
        destination: DestinationAddress,
        pkg: Package,
        packageWeight: Int,
        packageDescription: String,
        value: Float,
        apiAuthData: String?,
        rateCallback: Consumer<List<ShipmentRate>>,
        errorCallback: Consumer<String>
    ) {
        val ccShipment = ChitChatsShipment(
            name = unidecode(destination.name),
            address_1 = "${destination.addressLine1} ${destination.addressLine2}", //ChitChats seems to only accept line2 if it's put like this...
            address_2 = "",
            city = destination.city,
            province_code = destination.stateOrProvince,
            postal_code = destination.postalCode,
            country_code = destination.countryCode,
            phone = destination.phoneNumber,
            description = packageDescription,
            value = value,
            package_type = pkg.type,
            weight = packageWeight,
            size_x = pkg.length,
            size_y = pkg.width,
            size_z = pkg.height,
        )

        val client = ApiClient(apiAuthData)

        when (val shipmentResult = client.createShipmentAndGetId(ccShipment)) {
            is Success -> {
                val shipmentId = shipmentResult.value.shipment.id

                rateCallback.accept(shipmentResult.value.shipment.rates.map {
                    ShipmentRate(
                        shipmentId,
                        it.postageType,
                        it.postageDescription,
                        it.deliveryTimeDescription,
                        it.trackingTypeDescription,
                        it.isInsured,
                        it.paymentAmount
                    )
                })
            }
            is Failure -> errorCallback.accept(shipmentResult.reason.message ?: "")
        }
    }

    /**
     * Pay for shipment, and then poll ChitChats until the payment goes through
     * Then, return the Base64-encoded shipping label ZPL for printing
     */
    override suspend fun getShippingLabel(apiAuthData: String?, shipmentIdentifier: String, selectedOptionIdentifier: String, destination: DestinationAddress) : ShipmentInfo? {
        val client = ApiClient(apiAuthData)

        client.buyShipment(shipmentIdentifier, selectedOptionIdentifier)

        var result = client.getShipment(shipmentIdentifier)
        while (result?.shipment?.status == "postage_requested") {
            delay(500)
            result = client.getShipment(shipmentIdentifier)
        }

        return if (result?.shipment?.status == "ready") {
            val labelZpl = client.getLabelFromUrl(result.shipment.postageLabelZplUrl)
            if (labelZpl != null)
                ShipmentInfo(shipmentIdentifier, labelZpl)
            else
                null
        } else {
            null
        }
    }

    override fun getTrackingUrl(trackingNumber: String): String {
        return "https://chitchats.com/tracking/$trackingNumber"
    }
}