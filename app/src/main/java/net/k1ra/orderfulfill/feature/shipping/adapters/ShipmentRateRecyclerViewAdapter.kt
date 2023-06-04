package net.k1ra.orderfulfill.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.databinding.ShipmentRateCardBinding
import net.k1ra.orderfulfill.feature.shipping.model.ShipmentRate

class ShipmentRateRecyclerViewAdapter(
    private val list: List<ShipmentRate>,
    private val rateListener: ShipmentRateListener
) : BaseRecyclerViewAdapter<ShipmentRateCardBinding, ShipmentRate>(list) {

    override val layoutId: Int = R.layout.shipment_rate_card

    override fun bind(binding: ShipmentRateCardBinding, item: ShipmentRate) {
        binding.apply {
            rate = item
            listener = rateListener
            executePendingBindings()
        }
    }
}

interface ShipmentRateListener {
    fun onClicked(rate: ShipmentRate)
}