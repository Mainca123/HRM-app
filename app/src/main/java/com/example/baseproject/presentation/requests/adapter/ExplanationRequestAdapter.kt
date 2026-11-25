package com.example.baseproject.presentation.requests.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.remote.dto.attendance.RegisterLateEarlyDto
import com.example.baseproject.databinding.ItemLeaveRequestBinding

class ExplanationRequestAdapter(
    private var items: List<RegisterLateEarlyDto> = emptyList(),
    private val onItemClick: ((RegisterLateEarlyDto) -> Unit)? = null
) : RecyclerView.Adapter<ExplanationRequestAdapter.ViewHolder>() {

    fun submitList(newItems: List<RegisterLateEarlyDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaveRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemLeaveRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RegisterLateEarlyDto) {
            val typeTitle = item.typeName ?: if (item.type) "Về sớm" else "Đi muộn"
            binding.tvLeaveTypeName.text = typeTitle

            val dateStr = item.fromDate.take(10)
            val reasonStr = if (!item.reason.isNullOrBlank()) " · ${item.reason}" else ""
            binding.tvLeaveDateRange.text = "$dateStr$reasonStr"

            val isApproved = !item.modifiedUserName.isNullOrBlank()
            if (isApproved) {
                binding.tvStatusBadge.text = "Đã duyệt"
                binding.tvStatusBadge.setTextColor(0xFF15803D.toInt())
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFDCFCE7.toInt())
            } else {
                binding.tvStatusBadge.text = "Chờ duyệt"
                binding.tvStatusBadge.setTextColor(0xFFD97706.toInt())
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEF3C7.toInt())
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
