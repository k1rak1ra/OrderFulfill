package net.k1ra.orderfulfill.feature.label_printing.ui

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.PrinterRecyclerViewAdapter
import net.k1ra.orderfulfill.adapters.PrinterSetupListener
import net.k1ra.orderfulfill.databinding.PrinterOverlayBinding
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.feature.label_printing.model.Printer
import net.k1ra.orderfulfill.feature.orders.viewmodel.PlatformPendingOrderViewModel


class PrinterOverlay : DialogFragment() {
    private lateinit var binding: PrinterOverlayBinding
    private var viewModel: PlatformPendingOrderViewModel? = null
    private var shippingLabelZpl: String? = null
    private var dismissCallback: Runnable? = null
    private var order: PendingOrders.PendingOrder? = null

    companion object {
        private const val TAG = "PrintingOverlay"
        private var instance: PrinterOverlay? = null

        /**
         * Use a global instance without creating a new one
         * Note: the instance will be automatically cleared when it is destroyed
         */
        fun getInstance(): PrinterOverlay {
            if (instance == null) {
                instance = PrinterOverlay()
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
        binding = PrinterOverlayBinding.inflate(layoutInflater)
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
        viewModel: PlatformPendingOrderViewModel,
        shippingLabelZpl: String,
        order: PendingOrders.PendingOrder,
        dismissCallback: Runnable
    ) {
        this.viewModel = viewModel
        this.shippingLabelZpl = shippingLabelZpl
        this.order = order
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
        shippingLabelZpl ?: return
        viewModel ?: return
        order ?: return

        binding.btnDone.isEnabled = false
        binding.btnDone.setOnClickListener {
            viewModel!!.removeOrder(order!!)
            dismissCallback?.run()
        }

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
                viewModel!!.deletePrinter(
                    requireContext(),
                    viewModel!!.availablePrinters.value!![viewHolder.adapterPosition].id
                )
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(binding.savedPrinterRv)

        binding.savedPrinterRv.layoutManager = LinearLayoutManager(requireContext())
        viewModel!!.fetchPrinters(requireContext())
        viewModel!!.availablePrinters.observeForever {
            binding.savedPrinterRv.adapter = PrinterRecyclerViewAdapter(it, object: PrinterSetupListener{
                override fun onClicked(printer: Printer) {
                    viewModel!!.printLabel(shippingLabelZpl!!, printer.ip, requireContext())
                    binding.btnDone.isEnabled = true
                }
            })
        }

        binding.btnPrint.setOnClickListener {
            if (binding.printerName.text.toString().isNotEmpty() && !viewModel!!.availablePrinters.value!!.any { it.ip == binding.printerIpInput.text.toString() }) {
                viewModel!!.addNewPrinter(requireContext(), Printer(0, binding.printerIpInput.text.toString(), binding.printerName.text.toString()))
                viewModel!!.fetchPrinters(requireContext())
            }
            viewModel!!.printLabel(shippingLabelZpl!!, binding.printerIpInput.text.toString(), requireContext())
            binding.btnDone.isEnabled = true
        }
    }
}