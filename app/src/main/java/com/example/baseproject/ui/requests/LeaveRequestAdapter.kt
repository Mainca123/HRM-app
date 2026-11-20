package com.example.baseproject.ui.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.R
import com.example.baseproject.data.model.leave.LeaveRequestDto
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
            val to = item.toDate.take(10)
            binding.tvLeaveDateRange.text = if (from == to) {
                "$from · ${item.daysCount} ngày"
            } else {
                "$from - $to · ${item.daysCount} ngày"
            }

            when (item.status.uppercase()) {
                "APPROVED" -> {
                    binding.tvStatusBadge.text = "Đã duyệt"
                    binding.tvStatusBadge.setTextColor(0xFF15803D.toInt())
                    binding.tvStatusBadge.backgroundTintList =
                        ContextCompat.getColorStateList(itemView.context, R.color.card_bg)?.withAlpha(255)
                    binding.tvStatusBadge.setBackgroundColor(0xFFF0FDF4.toInt())
                }
                "REJECTED" -> {
                    binding.tvStatusBadge.text = "Từ chối"
                    binding.tvStatusBadge.setTextColor(0xFFDC2626.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFEF2F2.toInt())
                }
                "CANCELLED" -> {
                    binding.tvStatusBadge.text = "Đã hủy"
                    binding.tvStatusBadge.setTextColor(0xFF64748B.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFF1F5F9.toInt())
                }
                else -> {
                    binding.tvStatusBadge.text = "Chờ duyệt"
                    binding.tvStatusBadge.setTextColor(0xFFB45309.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFFFBEB.toInt())
                }
            }

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
