package net.k1ra.orderfulfill.feature.shipping.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.BaseRecyclerViewAdapter
import net.k1ra.orderfulfill.feature.shipping.model.Package
import net.k1ra.orderfulfill.databinding.PackageCardBinding

class PackageRecyclerViewAdapter(
    private val list: List<Package>
) : BaseRecyclerViewAdapter<PackageCardBinding, Package>(list) {

    override val layoutId: Int = R.layout.package_card

    override fun bind(binding: PackageCardBinding, item: Package) {
        binding.apply {
            pkg = item
            executePendingBindings()
        }
    }
}