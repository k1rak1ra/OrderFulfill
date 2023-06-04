package net.k1ra.orderfulfill.platforms.ecommerce

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import net.k1ra.orderfulfill.feature.orders.ui.PlatformPendingOrderFragment
import net.k1ra.orderfulfill.model.DataWorker
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.model.PlatformAuthorizationType
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import java.util.function.Consumer

interface EcomPlatformActions {
    //Platform info
    val type: Platform
    fun getPlatformImage(context: Context) : Bitmap

    //API setup related
    val authorizationType: PlatformAuthorizationType
    val oAuthCSRFstate: String
    val oAuthUrl: String
    val oAuthReturnUrl: String
    val oAuthAccessTokenAcquisitionRunnable: Consumer<DataWorker<HashMap<String, String>, String>> //Gets Oauth authorization code, returns access code
    fun apiKeyEntryDialog(fragment: PlatformPendingOrderFragment, dw: DataWorker<Unit, String>) : Dialog?

    //Order fulfillment related
    suspend fun getPendingOrders(apiAuthData: String, context: Context) : PendingOrders
    suspend fun updateTracking(apiAuthData: String, context: Context, order: PendingOrders.PendingOrder, trackingNumber: String?, carrier: ShippingPlatformActions) : Boolean
}