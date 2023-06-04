package net.k1ra.orderfulfill.platforms.shipping

import android.app.Dialog
import android.content.Context
import net.k1ra.orderfulfill.feature.shipping.model.DestinationAddress
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentInfo
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate
import java.util.*
import java.util.function.Consumer

class LettermailActions : ShippingPlatformActions {
    override val type = Platform.LETTERMAIL
    override val requiresApiAuth = false
    override val code = "Lettermail"

    //No API auth required so no need for this
    override fun apiKeyEntryDialog(context: Context, dw: DataWorker<Unit, String>): Dialog? {
        return null
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
        rateCallback.accept(listOf(
            ShipmentRate(
            "",
            "",
            "Lettermail",
            "3-6 days",
            "Not tracked",
            false,
            "1"
        )
        ))
    }

    override suspend fun getShippingLabel(apiAuthData: String?, shipmentIdentifier: String, selectedOptionIdentifier: String, destination: DestinationAddress) : ShipmentInfo {
        return ShipmentInfo(
            null,
            Base64.getEncoder().encodeToString(zplAddressLabel(destination).toByteArray())
        )
    }

    //This is lettermail so no tracking url
    override fun getTrackingUrl(trackingNumber: String): String {
        return ""
    }

    fun zplAddressLabel(destination: DestinationAddress) : String {
        val sb = StringBuilder()
        var index = 450

        for (line in destination.toString().split("\n")) {
            sb.append("^FO$index,550^ARR,1,1^FD$line^FS\n")
            index -= 25
        }

        return ("^XA\n" +
                "\n" +
                "\n" +
                "^CFA,30\n" +
                "^FO770,30^ARR,1,1^FDStandOutStickerCo^FS\n" +
                "^FO740,30^ARR,1,1^FD34 Grassmere Crt.^FS\n" +
                "^FO715,30^ARR,1,1^FDOshawa, ON^FS\n" +
                "^FO690,30^ARR,1,1^FDL1H 3X4^FS\n" +
                "\n" +
                "$sb" +
                "^MMT,N\n" +
                "^XZ")
    }
}