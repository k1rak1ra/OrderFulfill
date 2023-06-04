package net.k1ra.orderfulfill.feature.shipping.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.ShipmentRateListener
import net.k1ra.orderfulfill.adapters.ShipmentRateRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.SelectShippingOptionOverlayBinding
import net.k1ra.orderfulfill.feature.label_printing.ui.PrinterOverlay
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.feature.orders.viewmodel.PlatformPendingOrderViewModel


class SelectShippingOptionOverlay : DialogFragment() {
    private lateinit var binding: SelectShippingOptionOverlayBinding
    private var order: PendingOrders.PendingOrder? = null
    private var viewModel: PlatformPendingOrderViewModel? = null
    private var frgManager: FragmentManager? = null
    private var packageWeight: Int? = null
    private var packageDescription: String? = null
    private var pkg: Package? = null
    private var platform: ShippingPlatformActions? = null
    private var ecomPlatform: EcomPlatformActions? = null
    private var dismissCallback: Runnable? = null

    companion object {
        private const val TAG = "SelectShippingOverlay"
        private var instance: SelectShippingOptionOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): SelectShippingOptionOverlay {
            if (instance == null) {
                instance = SelectShippingOptionOverlay()
            }
            return instance!!
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.let {
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = SelectShippingOptionOverlayBinding.inflate(layoutInflater)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(requireContext().getColor(R.color.background)))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateView()
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Show overlay with passed data
     */
    fun showOverlay(
        frgManager: FragmentManager,
        order: PendingOrders.PendingOrder,
        viewModel: PlatformPendingOrderViewModel,
        packageWeight: Int,
        packageDescription: String,
        pkg: Package,
        platform: ShippingPlatformActions,
        ecomPlatform: EcomPlatformActions,
        dismissCallback: Runnable
    ) {
        this.order = order
        this.viewModel = viewModel
        this.frgManager = frgManager
        this.packageWeight = packageWeight
        this.packageDescription = packageDescription
        this.pkg = pkg
        this.platform = platform
        this.ecomPlatform = ecomPlatform
        this.dismissCallback = Runnable {
            hide()
            dismissCallback.run()
        }

        if (isResumed) {
            updateView()
            return
        } else {
            hide()
            show(frgManager, TAG)
        }
    }

    /**
     * Hide the overlay
     */
    fun hide() {
        try {
            dismissAllowingStateLoss()
        } catch (e: Exception) {
            //Ignore
        }
    }


    private fun updateView() {
        //If no data has even been passed, abort
        order ?: return
        viewModel ?: return
        packageWeight?: return
        packageDescription ?: return
        pkg ?: return
        platform ?: return
        ecomPlatform ?: return

        binding.shippingOptionsRecyclerview.layoutManager = LinearLayoutManager(requireContext())

        viewModel!!.getShippingRates(
            order!!,
            packageWeight!!,
            packageDescription!!,
            pkg!!,
            platform!!,
            requireContext(),
            {
                binding.shippingOptionsRecyclerview.adapter = ShipmentRateRecyclerViewAdapter(it, object: ShipmentRateListener{
                    override fun onClicked(rate: ShipmentRate) {
                        viewModel!!.buyRateAndUpdateTracking(
                            rate,
                            platform!!,
                            order!!,
                            ecomPlatform!!,
                            requireContext()
                        ) { shipmentInfo ->
                            PrinterOverlay.getInstance().showOverlay(frgManager!!, viewModel!!, shipmentInfo.shippingLabel, order!!, dismissCallback!!)
                        }
                    }
                })

                if(it.isEmpty())
                    binding.shippingOptionsNone.visibility = View.VISIBLE
                else
                    binding.shippingOptionsNone.visibility = View.GONE
            }
        ) {
            hide()
        }
    }
}