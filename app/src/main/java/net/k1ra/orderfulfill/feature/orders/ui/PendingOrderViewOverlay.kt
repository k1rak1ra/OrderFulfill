package net.k1ra.orderfulfill.feature.orders.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.feature.orders.adapters.ProductCardRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.PendingOrderViewOverlayBinding
import net.k1ra.orderfulfill.feature.shipping.ui.SelectShippingOptionOverlay
import net.k1ra.orderfulfill.feature.shipping.ui.ShippingSetupOverlay
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.feature.orders.viewmodel.PlatformPendingOrderViewModel


class PendingOrderViewOverlay : DialogFragment() {
    private lateinit var binding: PendingOrderViewOverlayBinding
    private var order: PendingOrders.PendingOrder? = null
    private var viewModel: PlatformPendingOrderViewModel? = null
    private var frgManager: FragmentManager? = null
    private var ecomPlatform: EcomPlatformActions? = null
    private var dismissCallback: Runnable? = null

    companion object {
        private const val TAG = "PendingOrderOverlay"
        private var instance: PendingOrderViewOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): PendingOrderViewOverlay {
            if (instance == null) {
                instance = PendingOrderViewOverlay()
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
        binding = PendingOrderViewOverlayBinding.inflate(layoutInflater)
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
        ecomPlatform: EcomPlatformActions
    ) {
        this.order = order
        this.viewModel = viewModel
        this.frgManager = frgManager
        this.ecomPlatform = ecomPlatform
        this.dismissCallback = Runnable {
            hide()
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
        ecomPlatform ?: return

        binding.orderOverlayOrderFor.text = requireContext().getString(R.string.order_for, order!!.recipient.name)
        binding.pendingOrderCreatedDate.text = order!!.createdStr
        binding.pendingOrderPaymentStatus.text = order!!.paymentState.toString()
        binding.pendingOrderContents.layoutManager = LinearLayoutManager(requireContext())
        binding.pendingOrderContents.adapter = ProductCardRecyclerViewAdapter(order!!.products)
        binding.customerPaidShipping.text = requireContext().getString(R.string.dollar_amount, order!!.shippingCost)
        binding.packageDestinationAddress.text = order!!.recipient.toString()

        viewModel!!.packages.observeForever {
            binding.packageTypeSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewModel!!.packages.value!!.map { it.name })
        }

        viewModel!!.availableShippingPlatforms.observeForever{
            binding.shippingProviderSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, viewModel!!.availableShippingPlatforms.value!!.map { it.type.toString() })
        }

        viewModel!!.populateSavedPackageContentDescription(requireContext(), ecomPlatform!!) {
            binding.packageContentsDescription.setText(it)
        }

        binding.btnShipmentCont.setOnClickListener {
            viewModel!!.validateShippingOptionsAndContinue(
                binding.packageTypeSpinner.selectedItem as String,
                binding.packageWeight.text.toString(),
                requireContext()
            ) {
                viewModel!!.savePackageContentDescription(requireContext(), ecomPlatform!!, binding.packageContentsDescription.text.toString())

                SelectShippingOptionOverlay.getInstance().showOverlay(
                    frgManager!!,
                    order!!,
                    viewModel!!,
                    binding.packageWeight.text.toString().toInt(),
                    binding.packageContentsDescription.text.toString(),
                    viewModel!!.packages.value!![binding.packageTypeSpinner.selectedItemPosition],
                    viewModel!!.availableShippingPlatforms.value!![binding.shippingProviderSpinner.selectedItemPosition],
                    ecomPlatform!!,
                    dismissCallback!!
                )
            }
        }

        binding.btnShipmentSetup.setOnClickListener {
            ShippingSetupOverlay.getInstance().showOverlay(frgManager!!, viewModel!!)
        }
    }
}