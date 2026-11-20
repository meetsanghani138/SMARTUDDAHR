package com.marwadiuniversity.radhe.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.marwadiuniversity.radhe.databinding.ItemStaffBinding
import com.marwadiuniversity.radhe.models.Staff

class StaffAdapter(
    private var list: List<Staff>
) : RecyclerView.Adapter<StaffAdapter.StaffViewHolder>() {

    fun updateList(newList: List<Staff>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class StaffViewHolder(private val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Staff) {
            binding.tvStaffName.text = item.name
            binding.tvStaffPhone.text = item.phone
            binding.tvStaffRole.text = item.role
        }
    }
}
