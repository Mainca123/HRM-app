package com.example.baseproject.ui.requests

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.databinding.ItemOvertimeRequestBinding
import java.util.Locale

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
            // Định dạng ngày theo Figma: 12/11/2026 · OT1
            val dateStr = formatDisplayDate(item.fromDate)
            val otCode = "OT${item.type}"
            binding.tvWorkDate.text = "$dateStr · $otCode"

            // Tính thời gian kết thúc dựa trên giờ bắt đầu mặc định (17:00) theo số giờ làm thêm
            val hours = item.hours
            val endMinutesTotal = (17 * 60 + (hours * 60).toInt())
            val endHour = endMinutesTotal / 60
            val endMin = endMinutesTotal % 60
            val endTimeStr = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMin)
            val hoursFormatted = if (hours % 1.0 == 0.0) "${hours.toInt()} giờ" else "$hours giờ"
            binding.tvTimeRange.text = "17:00 - $endTimeStr · $hoursFormatted"

            binding.tvReason.visibility = View.GONE

            if (item.isApproved) {
                binding.tvStatusBadge.text = "Đã duyệt"
                binding.tvStatusBadge.setTextColor(0xFF16A34A.toInt())
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFDCFCE7.toInt())
            } else {
                binding.tvStatusBadge.text = "Chờ duyệt"
                binding.tvStatusBadge.setTextColor(0xFFD97706.toInt())
                binding.tvStatusBadge.backgroundTintList = ColorStateList.valueOf(0xFFFEF3C7.toInt())
            }
        }

        private fun formatDisplayDate(dateStr: String?): String {
            if (dateStr.isNullOrBlank()) return ""
            return try {
                val clean = dateStr.take(10)
                val parts = clean.split("-")
                if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else clean
            } catch (_: Exception) {
                dateStr
            }
        }
    }
}
