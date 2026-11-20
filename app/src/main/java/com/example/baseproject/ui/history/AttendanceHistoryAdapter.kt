package com.example.baseproject.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.databinding.ItemAttendanceHistoryBinding

class AttendanceHistoryAdapter(
    private var items: List<TimeInTimeOutDto> = emptyList()
) : RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder>() {

    fun submitList(newItems: List<TimeInTimeOutDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceHistoryBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemAttendanceHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeInTimeOutDto) {
            binding.tvDate.text = item.workingDay.take(10)
            binding.tvShift.text = "Ca: ${item.shiftName ?: item.shiftId.ifBlank { "Hành chính" }}"

            val inStr = item.timeIn?.takeLast(8) ?: "--:--:--"
            val outStr = item.timeOut?.takeLast(8) ?: "--:--:--"
            binding.tvTimeIn.text = "Vào: $inStr"
            binding.tvTimeOut.text = "Ra: $outStr"

            val hours = item.workingHours ?: 0.0
            binding.tvWorkingHours.text = String.format("%.1f giờ", hours)

            // Status chip
            val late = item.lateMinutes ?: 0
            val early = item.earlyMinutes ?: 0

            when {
                item.timeIn.isNullOrBlank() && item.timeOut.isNullOrBlank() -> {
                    binding.tvStatusBadge.text = "Vắng"
                    binding.tvStatusBadge.setTextColor(0xFFDC2626.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFEF2F2.toInt())
                }
                late > 0 -> {
                    binding.tvStatusBadge.text = "Muộn ${late}p"
                    binding.tvStatusBadge.setTextColor(0xFFB45309.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFFFBEB.toInt())
                }
                early > 0 -> {
                    binding.tvStatusBadge.text = "Sớm ${early}p"
                    binding.tvStatusBadge.setTextColor(0xFFB45309.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFFFFBEB.toInt())
                }
                else -> {
                    binding.tvStatusBadge.text = "Đúng giờ"
                    binding.tvStatusBadge.setTextColor(0xFF15803D.toInt())
                    binding.tvStatusBadge.setBackgroundColor(0xFFF0FDF4.toInt())
                }
            }
        }
    }
}
