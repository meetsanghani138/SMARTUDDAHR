package com.marwadiuniversity.radhe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.radhe.databinding.ItemBillItemBinding
import com.marwadiuniversity.radhe.models.TransactionItem
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class BillItemAdapter(
    private val list: List<TransactionItem>
) : RecyclerView.Adapter<BillItemAdapter.BillViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class BillViewHolder(private val binding: ItemBillItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionItem) {
            binding.tvItemName.text = item.productName
            binding.tvItemQty.text = "${item.quantity} x ${CurrencyFormatter.formatPaiseToRupees(item.unitPricePaise)}"
            binding.tvItemTotal.text = CurrencyFormatter.formatPaiseToRupees(item.totalPricePaise)
        }
    }
}
