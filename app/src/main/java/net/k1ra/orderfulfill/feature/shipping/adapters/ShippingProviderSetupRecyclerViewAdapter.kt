package net.k1ra.orderfulfill.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.databinding.ShippingProviderCardBinding
import net.k1ra.orderfulfill.platforms.shipping.ShippingPlatformActions

class ShippingProviderSetupRecyclerViewAdapter(
    private val list: List<ShippingPlatformActions>,
    private val providerListener: ShippingProviderSetupListener
) : BaseRecyclerViewAdapter<ShippingProviderCardBinding, ShippingPlatformActions>(list) {

    override val layoutId: Int = R.layout.shipping_provider_card

    override fun bind(binding: ShippingProviderCardBinding, item: ShippingPlatformActions) {
        binding.apply {
            provider = item
            listener = providerListener
            executePendingBindings()
        }
    }
}

interface ShippingProviderSetupListener {
    fun onClicked(provider: ShippingPlatformActions)
}