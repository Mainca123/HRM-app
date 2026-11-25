package com.example.baseproject.presentation.management

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.databinding.ItemApprovalRequestBinding

class ApprovalAdapter(
    private var items: List<LeaveRequestDto> = emptyList(),
    private val onApproveClick: (LeaveRequestDto) -> Unit,
    private val onRejectClick: (LeaveRequestDto) -> Unit
) : RecyclerView.Adapter<ApprovalAdapter.ViewHolder>() {

    fun submitList(newItems: List<LeaveRequestDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApprovalRequestBinding.inflate(
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

    inner class ViewHolder(private val binding: ItemApprovalRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LeaveRequestDto) {
            val name = item.employeeFullName ?: "Nhân viên #${item.employeeId}"
            binding.tvEmployeeName.text = name
            val parts = name.trim().split(" ")
            binding.tvAvatarInitials.text = if (parts.size >= 2) {
                "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
            } else {
                name.take(2).uppercase()
            }

            val code = item.employeeCode ?: "NV${item.employeeId}"
            val dept = item.departmentName ?: "Phòng ban"
            binding.tvEmployeeCodeDept.text = "$code · $dept"

            val typeName = item.absenceTypeName.ifBlank { "Nghỉ phép" }
            val from = item.fromDate.take(10)
            val to = item.toDate?.take(10) ?: from
            binding.tvRequestDetails.text = when {
                item.absenceId.startsWith("OT:") -> "Loại: $typeName · $from (${item.unit ?: 0.0} giờ)"
                item.absenceId.startsWith("EXPLAIN:") -> "Loại: $typeName · $from"
                else -> "Loại: $typeName · $from - $to (${item.daysCount} ngày)"
            }
            binding.tvRequestReason.text = "Lý do: ${item.reason.ifBlank { "Không có ghi chú" }}"

            binding.btnApprove.setOnClickListener {
                onApproveClick(item)
            }

            binding.btnReject.setOnClickListener {
                onRejectClick(item)
            }
        }
    }
}
