package com.marwadiuniversity.radhe.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.radhe.R
import com.marwadiuniversity.radhe.databinding.ItemTransactionBinding
import com.marwadiuniversity.radhe.models.TransactionRecord
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class TransactionAdapter(
    private var list: List<TransactionRecord>,
    private val onItemClick: (TransactionRecord) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TxnViewHolder>() {

    fun updateList(newList: List<TransactionRecord>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxnViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TxnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TxnViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class TxnViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TransactionRecord) {
            val context = itemView.context
            binding.tvCustomerName.text = item.customerName.ifBlank { "Walk-in Customer" }
            binding.tvTxnMeta.text = "${item.transactionNumber} • ${item.createdAt}"
            binding.tvRunningBalance.text = "Bal: ${CurrencyFormatter.formatPaiseToRupees(item.newBalancePaise)}"
            binding.tvAmount.text = CurrencyFormatter.formatPaiseToRupees(item.totalAmountPaise)

            if (item.type == "CREDIT_PURCHASE") {
                binding.tvTypeBadge.text = "UDHAR"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_red)
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.danger_color))
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.danger_color))
                binding.ivTxnType.setImageResource(R.drawable.ic_udhar)
                binding.ivTxnType.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FEE2E2"))
                binding.ivTxnType.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.danger_color))
            } else {
                binding.tvTypeBadge.text = "PAYMENT"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_green)
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.primary_green))
                binding.tvAmount.setTextColor(ContextCompat.getColor(context, R.color.primary_green))
                binding.ivTxnType.setImageResource(R.drawable.ic_payment)
                binding.ivTxnType.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.light_green))
                binding.ivTxnType.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary_green))
            }

            if (item.isReversed) {
                binding.tvTypeBadge.text = "REVERSED"
                binding.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_orange)
                binding.tvTypeBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_color))
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
