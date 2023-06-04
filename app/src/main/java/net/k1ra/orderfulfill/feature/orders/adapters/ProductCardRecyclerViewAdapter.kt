package net.k1ra.orderfulfill.feature.orders.adapters

import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.BaseRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.ProductCardBinding
import net.k1ra.orderfulfill.feature.orders.model.Product

class ProductCardRecyclerViewAdapter(
    private val list: List<Product>
) : BaseRecyclerViewAdapter<ProductCardBinding, Product>(list) {

    override val layoutId: Int = R.layout.product_card

    override fun bind(binding: ProductCardBinding, item: Product) {
        binding.apply {
            product = item
            executePendingBindings()
        }
    }
}