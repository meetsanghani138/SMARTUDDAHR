package com.marwadiuniversity.radhe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.radhe.databinding.ItemUdharProductBinding
import com.marwadiuniversity.radhe.models.Product
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class UdharProductAdapter(
    private var list: List<Product>,
    private val quantities: MutableMap<Long, Int>,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<UdharProductAdapter.UdharViewHolder>() {

    fun updateList(newList: List<Product>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UdharViewHolder {
        val binding = ItemUdharProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UdharViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UdharViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class UdharViewHolder(private val binding: ItemUdharProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Product) {
            binding.tvProductName.text = item.name
            binding.tvProductPrice.text = CurrencyFormatter.formatPaiseToRupees(item.sellingPricePaise)

            val qty = quantities[item.id] ?: 0
            binding.tvQuantity.text = qty.toString()

            binding.btnPlus.setOnClickListener {
                val current = quantities[item.id] ?: 0
                val next = current + 1
                quantities[item.id] = next
                binding.tvQuantity.text = next.toString()
                onQuantityChanged()
            }

            binding.btnMinus.setOnClickListener {
                val current = quantities[item.id] ?: 0
                if (current > 0) {
                    val next = current - 1
                    quantities[item.id] = next
                    binding.tvQuantity.text = next.toString()
                    onQuantityChanged()
                }
            }
        }
    }
}
