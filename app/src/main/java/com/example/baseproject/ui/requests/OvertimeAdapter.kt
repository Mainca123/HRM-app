package com.example.baseproject.ui.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.databinding.ItemOvertimeRequestBinding

class OvertimeAdapter(
    private var items: List<OvertimeRequestDto> = emptyList()
) : RecyclerView.Adapter<OvertimeAdapter.ViewHolder>() {

    fun submitList(newItems: List<OvertimeRequestDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOvertimeRequestBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemOvertimeRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OvertimeRequestDto) {
            val type = item.overtimeTypeName ?: item.overtimeTypeId.ifBlank { "Tăng ca" }
            binding.tvWorkDate.text = "${item.workDate.take(10)} · $type"
            binding.tvTimeRange.text = "${item.startTime.take(5)} - ${item.endTime.take(5)} (${item.hours} giờ)"
            binding.tvReason.text = item.reason.ifBlank { "Tăng ca theo kế hoạch" }

            when (item.status.uppercase()) {
                "APPROVED" -> {
                    binding.tvStatusBadge.text = "Đã duyệt"
                    binding.tvStatusBadge.setTextColor(0xFF15803D.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFF0FDF4.toInt())
                }
                "REJECTED" -> {
                    binding.tvStatusBadge.text = "Từ chối"
                    binding.tvStatusBadge.setTextColor(0xFFDC2626.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFEF2F2.toInt())
                }
                else -> {
                    binding.tvStatusBadge.text = "Chờ duyệt"
                    binding.tvStatusBadge.setTextColor(0xFFB45309.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFFFBEB.toInt())
                }
            }
        }
    }
}
