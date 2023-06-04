package net.k1ra.orderfulfill.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.databinding.PrinterCardBinding
import net.k1ra.orderfulfill.feature.label_printing.model.Printer

class PrinterRecyclerViewAdapter(
    private val list: List<Printer>,
    private val printerListener: PrinterSetupListener
) : BaseRecyclerViewAdapter<PrinterCardBinding, Printer>(list) {

    override val layoutId: Int = R.layout.printer_card

    override fun bind(binding: PrinterCardBinding, item: Printer) {
        binding.apply {
            printer = item
            listener = printerListener
            executePendingBindings()
        }
    }
}

interface PrinterSetupListener {
    fun onClicked(printer: Printer)
}