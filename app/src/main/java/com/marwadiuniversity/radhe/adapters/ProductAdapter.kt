package com.marwadiuniversity.radhe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.databinding.ItemProductBinding
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class ProductAdapter(
    private var list: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateList(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product) {
            val context = itemView.context
            binding.tvProductName.text = item.name
            binding.tvCategoryName.text = "${item.categoryName.ifBlank { "General" }} • SKU: ${item.sku}"
            binding.tvSellingPrice.text = CurrencyFormatter.formatPaiseToRupees(item.sellingPricePaise)
            binding.tvStockQty.text = "Stock: ${item.stockQuantity} ${item.unit}"

            if (item.imagePath.isNotBlank()) {
                binding.ivProductImage.setPadding(0, 0, 0, 0)
                binding.ivProductImage.imageTintList = null
                Glide.with(context)
                    .load(item.imagePath)
                    .placeholder(R.drawable.ic_products)
                    .error(R.drawable.ic_products)
                    .into(binding.ivProductImage)
            } else {
                binding.ivProductImage.setImageResource(R.drawable.ic_products)
            }

            if (item.stockQuantity <= 10) {
                binding.tvStockQty.setTextColor(ContextCompat.getColor(context, R.color.danger_color))
            } else {
                binding.tvStockQty.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
