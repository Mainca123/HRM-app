package com.example.baseproject.presentation.requests.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.databinding.ItemLeaveRequestBinding

class LeaveRequestAdapter(
    private var items: List<LeaveRequestDto> = emptyList(),
    private val onItemClick: ((LeaveRequestDto) -> Unit)? = null
) : RecyclerView.Adapter<LeaveRequestAdapter.ViewHolder>() {

    fun submitList(newItems: List<LeaveRequestDto>) {
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

        fun bind(item: LeaveRequestDto) {
            binding.tvLeaveTypeName.text = item.absenceTypeName.ifBlank { "Nghỉ phép" }
            val from = item.fromDate.take(10)
            val to = item.toDate?.take(10) ?: from
            binding.tvLeaveDateRange.text = if (from == to) {
                "$from · ${item.daysCount} ngày"
            } else {
                "$from - $to · ${item.daysCount} ngày"
            }

            when (item.status?.toInt()) {
                1 -> {
                    binding.tvStatusBadge.text = "Đã duyệt"
                    binding.tvStatusBadge.setTextColor(0xFF15803D.toInt())
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFDCFCE7.toInt())
                }
                2 -> {
                    binding.tvStatusBadge.text = "Từ chối"
                    binding.tvStatusBadge.setTextColor(0xFFDC2626.toInt())
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEE2E2.toInt())
                }
                3 -> {
                    binding.tvStatusBadge.text = "Đã hủy"
                    binding.tvStatusBadge.setTextColor(0xFF64748B.toInt())
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFF1F5F9.toInt())
                }
                else -> {
                    binding.tvStatusBadge.text = "Chờ duyệt"
                    binding.tvStatusBadge.setTextColor(0xFFD97706.toInt())
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEF3C7.toInt())
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
