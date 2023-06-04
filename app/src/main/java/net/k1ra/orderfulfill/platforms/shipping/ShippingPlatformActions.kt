package net.k1ra.orderfulfill.platforms.shipping

import android.app.Dialog
import android.content.Context
import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentInfo
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate
import java.util.function.Consumer

interface ShippingPlatformActions {
    val type: Platform
    val requiresApiAuth: Boolean
    val code: String

    fun apiKeyEntryDialog(context: Context, dw: DataWorker<Unit, String>) : Dialog?

    suspend fun getShippingOptions(
        destination: DestinationAddress,
        pkg: Package,
        packageWeight: Int,
        packageDescription: String,
        value: Float,
        apiAuthData: String?,
        rateCallback: Consumer<List<ShipmentRate>>,
        errorCallback: Consumer<String>
    )

    suspend fun getShippingLabel(apiAuthData: String?, shipmentIdentifier: String, selectedOptionIdentifier: String, destination: DestinationAddress) : ShipmentInfo?

    fun getTrackingUrl(trackingNumber: String) : String
}