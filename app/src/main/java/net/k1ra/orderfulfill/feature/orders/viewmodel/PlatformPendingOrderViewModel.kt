package net.k1ra.orderfulfill.feature.orders.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.PlatformProvider
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.providers.zebra_printer.PrinterManager
import net.k1ra.orderfulfill.feature.shipping.model.PackageContentDescription
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.feature.label_printing.model.Printer
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentInfo
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate
import net.k1ra.orderfulfill.secure_storage.db.PlatformApiAuthData
import net.k1ra.orderfulfill.viewmodel.BaseDynamicViewModel
import java.util.function.Consumer

class PlatformPendingOrderViewModel : BaseDynamicViewModel() {
    //MutableLiveData emitted by the ViewModel that the Fragment listens to
    val orders = MutableLiveData<List<PendingOrders.PendingOrder>>(arrayListOf())
    val packages = MutableLiveData<List<Package>>(listOf())
    val availableShippingPlatforms = MutableLiveData<List<ShippingPlatformActions>>(listOf())
    val availablePrinters = MutableLiveData<List<Printer>>(listOf())

    //Callbacks for things that require UI interaction
    var apiAuthDataMissingOrInvalidCallback: Consumer<DataWorker<Unit, String>>? = null

    /**
     * Checks if API auth data is set or not before proceeding the loading unfulfilled orders
     */
    fun init(context: Context, platformActions: EcomPlatformActions) =
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(context.getString(R.string.loading_unfulfilled_orders))
            val apiAuthData = PlatformApiAuthData.retrieve(platformActions.type, context)

            if (apiAuthData == null)
                apiAuthDataInvalidError(context, platformActions)
            else
                loadUnfulfilledOrders(context, platformActions, apiAuthData)
        }

    private fun apiAuthDataInvalidError(context: Context, platformActions: EcomPlatformActions) {
        setErrorWithAction(context.getString(R.string.api_key_missing), true) {
            Handler(Looper.getMainLooper()).post {
                apiAuthDataMissingOrInvalidCallback?.accept(DataWorker(Unit) {
                    PlatformApiAuthData.store(platformActions.type, it.data, context)
                })
            }
        }
    }

    fun storeApiAuthDataForShippingPlatforms(context: Context, data: String, platform: Platform) = viewModelScope.launch(Dispatchers.IO) {
            PlatformApiAuthData.store(platform, data, context)
            updateAvailableShippingPlatforms(context)
        }

    /**
     * Loads unfulfilled orders for the platform and then updates the list
     */
    private fun loadUnfulfilledOrders(
        context: Context,
        platformActions: EcomPlatformActions,
        apiAuthData: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = platformActions.getPendingOrders(apiAuthData, context)) {
                is PendingOrders.Success -> {
                    updateAvailablePackages(context)
                    updateAvailableShippingPlatforms(context)
                    withContext(Dispatchers.Main) {
                        orders.value = result.orders
                        setComplete()
                    }
                }
                is PendingOrders.Failure -> {
                    when (result.type) {
                        RequestFailureType.AUTH_INVALID -> {
                            apiAuthDataInvalidError(context, platformActions)
                        }
                        RequestFailureType.OTHER -> {
                            setErrorWithAction(result.message, false) {
                                loadUnfulfilledOrders(context, platformActions, apiAuthData)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateAvailablePackages(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        val packagesList = Package.getDb(context).getAll()
        withContext(Dispatchers.Main) {
            packages.value = packagesList
        }
    }

    private fun updateAvailableShippingPlatforms(context: Context) = viewModelScope.launch(Dispatchers.IO) {
            val list = PlatformProvider.getShippingTypes()
            list.removeIf {
                it.requiresApiAuth && PlatformApiAuthData.retrieve(
                    it.type,
                    context
                ) == null
            }
            withContext(Dispatchers.Main) {
                availableShippingPlatforms.value = list
            }
        }

    fun addNewPackage(
        name: String,
        length: String,
        width: String,
        height: String,
        type: String,
        context: Context,
        successCallback: Runnable
    ) = viewModelScope.launch(Dispatchers.IO) {
        when {
            name.isEmpty() -> {
                setErrorWithAction(context.getString(R.string.pkg_name_cannot_be_empty), false) {
                    setComplete()
                }
            }
            length.toFloatOrNull() == null || width.toFloatOrNull() == null || height.toFloatOrNull() == null -> {
                setErrorWithAction(context.getString(R.string.pkg_dims_invalid), false) {
                    setComplete()
                }
            }
            else -> {
                val pkg = Package(
                    0,
                    length.toFloat(),
                    width.toFloat(),
                    height.toFloat(),
                    type,
                    name
                )
                Package.getDb(context).insert(pkg)
                updateAvailablePackages(context)
                Handler(Looper.getMainLooper()).post {
                    successCallback.run()
                }
            }
        }
    }

    fun deletePackage(id: Int, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        Package.getDb(context).delete(id)
        updateAvailablePackages(context)
    }

    fun validateShippingOptionsAndContinue(
        pkgType: String,
        weight: String,
        context: Context,
        successCallback: Runnable
    ) {
        when {
            pkgType.isEmpty() -> {
                setErrorWithAction(context.getString(R.string.select_pkg_type), false) {
                    setComplete()
                }
            }
            weight.toIntOrNull() == null -> {
                setErrorWithAction(context.getString(R.string.pkg_weight_invalid), false) {
                    setComplete()
                }
            }
            else -> {
                successCallback.run()
            }
        }
    }

    fun getShippingRates(
        order: PendingOrders.PendingOrder,
        packageWeight: Int,
        packageDescription: String,
        pkg: Package,
        platform: ShippingPlatformActions,
        context: Context,
        onRatesFetched: Consumer<List<ShipmentRate>>,
        onFail: Runnable
    ) = viewModelScope.launch(Dispatchers.IO) {
        setLoading(context.getString(R.string.loading_shipping_rates))

        platform.getShippingOptions(
            order.recipient,
            pkg,
            packageWeight,
            packageDescription,
            order.totalCost.toFloat(),
            PlatformApiAuthData.retrieve(platform.type, context),
            {
                setComplete()
                Handler(Looper.getMainLooper()).post {
                    onRatesFetched.accept(it)
                }
            }
        ) { error ->
            setErrorWithAction(error, false) {
                setComplete()
                onFail.run()
            }
        }
    }

    fun buyRateAndUpdateTracking(
        rate: ShipmentRate,
        shippingPlatform: ShippingPlatformActions,
        order: PendingOrders.PendingOrder,
        ecomPlatform: EcomPlatformActions,
        context: Context,
        successCallback: Consumer<ShipmentInfo>
    ) = viewModelScope.launch(Dispatchers.IO) {
        setLoading(context.getString(R.string.buying_rate))

        val label = shippingPlatform.getShippingLabel(
            PlatformApiAuthData.retrieve(shippingPlatform.type, context),
            rate.shipmentId,
            rate.rateId,
            order.recipient
        )

        /**
         * If label is created, then check if package is tracked
         * If package is tracked, update tracking with carrier code and tracking number
         * Otherwise, update tracking as none
         */
        if (label != null) {
            val success = ecomPlatform.updateTracking(
                PlatformApiAuthData.retrieve(ecomPlatform.type, context)!!,
                context,
                order,
                label.trackingNumber,
                shippingPlatform
            )

            if (success) {
                setComplete()
                Handler(Looper.getMainLooper()).post {
                    successCallback.accept(label)
                }
            } else {
                setErrorWithAction(context.getString(R.string.tracking_update_failed), false) {
                    setComplete()
                }
            }
        } else {
            setErrorWithAction(context.getString(R.string.buying_rate_failed), false) {
                setComplete()
            }
        }
    }

    fun printLabel(base64EncodedZpl: String, ip: String, context: Context) = viewModelScope.launch(Dispatchers.IO) {
        setLoading(context.getString(R.string.printing))
        try {
            PrinterManager().print(ip, base64EncodedZpl)
            setComplete()
        } catch (e: Exception) {
            setErrorWithAction(context.getString(R.string.printing_failed), false){
                setComplete()
            }
        }
    }

    fun populateSavedPackageContentDescription(context: Context, platform: EcomPlatformActions, successCallback: Consumer<String>) = viewModelScope.launch(Dispatchers.IO) {
        val description = PackageContentDescription.getDb(context).getById(platform.type.ordinal)?.description ?: ""
        Handler(Looper.getMainLooper()).post {
            successCallback.accept(description)
        }
    }

    fun savePackageContentDescription(context: Context, platformActions: EcomPlatformActions, description: String) = viewModelScope.launch(Dispatchers.IO) {
        val dao = PackageContentDescription.getDb(context)
        dao.delete(platformActions.type.ordinal)
        dao.insert(PackageContentDescription(platformActions.type.ordinal, description))
    }

    fun fetchPrinters(context: Context) = viewModelScope.launch {
        availablePrinters.value = withContext(Dispatchers.IO) { Printer.getDb(context).getAll() }!! //Double bang shouldn't be here but the IDE complains when it's missing
    }

    fun deletePrinter(context: Context, id: Int) = viewModelScope.launch(Dispatchers.IO) {
        Printer.getDb(context).delete(id)
        fetchPrinters(context)
    }

    fun addNewPrinter(context: Context, printer: Printer) = viewModelScope.launch(Dispatchers.IO) {
        Printer.getDb(context).insert(printer)
    }

    fun removeOrder(order: PendingOrders.PendingOrder) = viewModelScope.launch {
        val ordersAL = orders.value!! as ArrayList
        ordersAL.remove(order)
        orders.value = ordersAL
    }
}