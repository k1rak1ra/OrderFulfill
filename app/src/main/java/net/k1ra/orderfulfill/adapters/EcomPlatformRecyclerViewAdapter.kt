package net.k1ra.orderfulfill.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.databinding.EcomPlatformCardBinding
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions

class EcomPlatformRecyclerViewAdapter(
    private val list: List<EcomPlatformActions>,
    private val platformListener: EcomPlatformListener
) : BaseRecyclerViewAdapter<EcomPlatformCardBinding, EcomPlatformActions>(list) {

    override val layoutId: Int = R.layout.ecom_platform_card

    override fun bind(binding: EcomPlatformCardBinding, item: EcomPlatformActions) {
        binding.apply {
            platform = item
            listener = platformListener
            executePendingBindings()
        }
    }
}

interface EcomPlatformListener {
    fun onClicked(platform: EcomPlatformActions)
}