package net.k1ra.orderfulfill.feature.shipping.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.feature.shipping.adapters.PackageRecyclerViewAdapter
import net.k1ra.orderfulfill.adapters.ShippingProviderSetupListener
import net.k1ra.orderfulfill.adapters.ShippingProviderSetupRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.ShippingSetupOverlayBinding
import net.k1ra.orderfulfill.model.DataWorker
import net.k1ra.orderfulfill.feature.shipping.model.PackageTypes
import net.k1ra.orderfulfill.platforms.PlatformProvider
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions
import net.k1ra.orderfulfill.feature.orders.viewmodel.PlatformPendingOrderViewModel


class ShippingSetupOverlay : DialogFragment() {
    private lateinit var binding: ShippingSetupOverlayBinding
    private var viewModel: PlatformPendingOrderViewModel? = null

    companion object {
        private const val TAG = "PendingOrderOverlay"
        private var instance: ShippingSetupOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): ShippingSetupOverlay {
            if (instance == null) {
                instance = ShippingSetupOverlay()
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
        binding = ShippingSetupOverlayBinding.inflate(layoutInflater)
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
    fun showOverlay(frgManager: FragmentManager, viewModel: PlatformPendingOrderViewModel) {
        this.viewModel = viewModel
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
        viewModel ?: return

        binding.packagingTypeList.layoutManager = LinearLayoutManager(requireContext())
        binding.newPkgType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, PackageTypes.values().map { it.toString() })

        //Packaging swipe to delete
        val swipeToDeleteCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(
                0,
                 ItemTouchHelper.RIGHT
            ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                viewModel!!.deletePackage(
                    viewModel!!.packages.value!![viewHolder.adapterPosition].id,
                    requireContext()
                )
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.packagingTypeList)

        viewModel!!.packages.observeForever {
            binding.packagingTypeList.adapter = PackageRecyclerViewAdapter(viewModel!!.packages.value!!)
        }

        val shippingProviders = PlatformProvider.getShippingTypes()
        shippingProviders.removeIf { !it.requiresApiAuth }

        binding.shippingProviderSetup.layoutManager = LinearLayoutManager(requireContext())
        binding.shippingProviderSetup.adapter = ShippingProviderSetupRecyclerViewAdapter(shippingProviders, object: ShippingProviderSetupListener{
            override fun onClicked(provider: ShippingPlatformActions) {
                val callback = DataWorker<Unit, String>(Unit) {
                    viewModel!!.storeApiAuthDataForShippingPlatforms(requireContext(), it.data, provider.type)
                }

                provider.apiKeyEntryDialog(requireContext(), callback)?.show()
            }
        })

        binding.btnNewPkgAdd.setOnClickListener {
            viewModel!!.addNewPackage(
                binding.newPackageName.text.toString(),
                binding.newPackageLength.text.toString(),
                binding.newPackageWidth.text.toString(),
                binding.newPackageHeight.text.toString(),
                binding.newPkgType.selectedItem.toString(),
                requireContext()
            ) {
                binding.newPackageName.text.clear()
                binding.newPackageLength.text.clear()
                binding.newPackageWidth.text.clear()
                binding.newPackageHeight.text.clear()
            }
        }
    }
}