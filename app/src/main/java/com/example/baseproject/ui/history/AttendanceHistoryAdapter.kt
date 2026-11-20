package com.example.baseproject.ui.history

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.databinding.ItemAttendanceHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AttendanceHistoryAdapter(
    private var items: List<TimeInTimeOutDto> = emptyList()
) : RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder>() {

    private val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("vi", "VN"))
    private val dayMonthFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

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
            // Parse day of week and day/month
            try {
                val parsedDate = inputDateFormat.parse(item.workingDay.take(10))
                if (parsedDate != null) {
                    val dow = dayOfWeekFormat.format(parsedDate)
                        .replace("Thứ Hai", "Thứ 2")
                        .replace("Thứ Ba", "Thứ 3")
                        .replace("Thứ Tư", "Thứ 4")
                        .replace("Thứ Năm", "Thứ 5")
                        .replace("Thứ Sáu", "Thứ 6")
                        .replace("Thứ Bảy", "Thứ 7")
                        .replace("Chủ Nhật", "CN")
                    binding.tvDayOfWeek.text = dow
                    binding.tvDayAndMonth.text = dayMonthFormat.format(parsedDate)
                } else {
                    binding.tvDayOfWeek.text = "Thứ 2"
                    binding.tvDayAndMonth.text = item.workingDay.takeLast(5)
                }
            } catch (e: Exception) {
                binding.tvDayOfWeek.text = "Thứ 2"
                binding.tvDayAndMonth.text = item.workingDay.takeLast(5)
            }

            // Time in -> Time out
            val inStr = item.timeIn?.takeLast(8)?.take(5) ?: "--:--"
            val outStr = item.timeOut?.takeLast(8)?.take(5) ?: "--:--"
            binding.tvTimeInOut.text = "$inStr → $outStr"

            val shift = item.shiftName?.ifBlank { null } ?: "Ca HC"
            binding.tvShiftName.text = shift

            val hours = item.workingHours ?: 0.0
            val late = item.lateMinutes ?: 0
            val early = item.earlyMinutes ?: 0

            when {
                !item.timeIn.isNullOrBlank() && item.timeOut.isNullOrBlank() -> {
                    // Đang làm
                    binding.tvStatusBadge.text = "Đang làm"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFEFF6FF.toInt())
                    binding.tvStatusBadge.setTextColor(0xFF2563EB.toInt())
                    binding.tvWorkingHours.text = "Hôm nay"
                }
                item.timeIn.isNullOrBlank() && item.timeOut.isNullOrBlank() -> {
                    // Vắng mặt
                    binding.tvStatusBadge.text = "Vắng mặt"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEE2E2.toInt())
                    binding.tvStatusBadge.setTextColor(0xFFDC2626.toInt())
                    binding.tvWorkingHours.text = "0h"
                    binding.tvShiftName.text = "Không có dữ liệu quẹt thẻ"
                }
                late > 0 -> {
                    binding.tvStatusBadge.text = "Muộn ${late}p"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEF3C7.toInt())
                    binding.tvStatusBadge.setTextColor(0xFFD97706.toInt())
                    binding.tvWorkingHours.text = String.format(Locale.US, "%.1fh", hours)
                }
                early > 0 -> {
                    binding.tvStatusBadge.text = "Sớm ${early}p"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEF3C7.toInt())
                    binding.tvStatusBadge.setTextColor(0xFFD97706.toInt())
                    binding.tvWorkingHours.text = String.format(Locale.US, "%.1fh", hours)
                }
                else -> {
                    binding.tvStatusBadge.text = "Đúng giờ"
                    binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFDCFCE7.toInt())
                    binding.tvStatusBadge.setTextColor(0xFF16A34A.toInt())
                    binding.tvWorkingHours.text = String.format(Locale.US, "%.1fh", hours)
                }
            }
        }
    }
}