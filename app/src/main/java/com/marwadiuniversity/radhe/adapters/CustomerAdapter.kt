package com.marwadiuniversity.radhe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.radhe.databinding.ItemCustomerBinding
import com.marwadiuniversity.radhe.models.Customer
import com.marwadiuniversity.radhe.utils.CurrencyFormatter

class CustomerAdapter(
    private var list: List<Customer>,
    private val onItemClick: (Customer) -> Unit,
    private val onWhatsAppClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    fun updateList(newList: List<Customer>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Customer) {
            binding.tvCustomerName.text = item.name
            binding.tvCustomerMobile.text = item.mobile
            binding.tvAvatar.text = item.name.firstOrNull()?.uppercase() ?: "C"
            binding.tvOutstandingAmount.text = CurrencyFormatter.formatPaiseToRupees(item.currentOutstandingPaise)

            itemView.setOnClickListener { onItemClick(item) }
            binding.btnWhatsApp.setOnClickListener { onWhatsAppClick(item) }
        }
    }
}
