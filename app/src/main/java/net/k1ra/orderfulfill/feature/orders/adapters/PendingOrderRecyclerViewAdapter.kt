package net.k1ra.orderfulfill.feature.orders.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.BaseRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.OrderCardBinding
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders

class PendingOrderRecyclerViewAdapter(
    private val list: List<PendingOrders.PendingOrder>,
    private val orderListener: PendingOrderListener
) : BaseRecyclerViewAdapter<OrderCardBinding, PendingOrders.PendingOrder>(list) {

    override val layoutId: Int = R.layout.order_card

    override fun bind(binding: OrderCardBinding, item: PendingOrders.PendingOrder) {
        binding.apply {
            order = item
            listener = orderListener
            executePendingBindings()
        }
    }
}

interface PendingOrderListener {
    fun onOrderClicked(order: PendingOrders.PendingOrder)
}