package com.example.baseproject.ui.forms

import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.model.leave.AbsenceTypeDto
import com.example.baseproject.data.model.leave.CreateLeaveRequest
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.LeaveRepository
import com.example.baseproject.databinding.ActivityCreateLeaveBinding
import com.example.baseproject.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CreateLeaveActivity : BaseActivity<ActivityCreateLeaveBinding>() {

    @Inject
    lateinit var leaveRepository: LeaveRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private var absenceTypes: List<AbsenceTypeDto> = emptyList()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun provideViewBinding(): ActivityCreateLeaveBinding =
        ActivityCreateLeaveBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()

        val todayStr = dateFormat.format(calendar.time)
        viewBinding.etFromDate.setText(todayStr)
        viewBinding.etToDate.setText(todayStr)
        viewBinding.etDaysCount.setText("1.0")

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.etFromDate.setOnClickListener {
            showDatePicker { date ->
                viewBinding.etFromDate.setText(date)
            }
        }

        viewBinding.etToDate.setOnClickListener {
            showDatePicker { date ->
                viewBinding.etToDate.setText(date)
            }
        }

        viewBinding.btnSubmit.setOnClickListener {
            submitLeave()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load absence types
            when (val res = leaveRepository.getAbsenceTypes()) {
                is Resource.Success -> {
                    absenceTypes = res.data ?: emptyList()
                    val names = absenceTypes.map { it.name }
                    val adapter = ArrayAdapter(this@CreateLeaveActivity, android.R.layout.simple_spinner_dropdown_item, names)
                    viewBinding.spAbsenceType.adapter = adapter
                }
                else -> {}
            }

            // Load balance
            val user = authRepository.getUserProfile()
            user?.employeeId?.let { empId ->
                when (val balanceRes = leaveRepository.getLeaveBalance(empId)) {
                    is Resource.Success -> {
                        balanceRes.data?.let { b ->
                            viewBinding.tvLeaveBalanceBanner.text = "ⓘ  Phép năm còn lại: ${b.remainingLeaveDays} ngày (Đã dùng: ${b.usedLeaveDays})"
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day)
            onDateSelected(dateFormat.format(calendar.time))
        }, y, m, d).show()
    }

    private fun submitLeave() {
        if (absenceTypes.isEmpty()) {
            showToast("Đang tải danh mục loại phép, vui lòng thử lại")
            return
        }

        val selectedIndex = viewBinding.spAbsenceType.selectedItemPosition
        val absenceType = absenceTypes.getOrNull(selectedIndex) ?: absenceTypes.first()
        val fromDate = viewBinding.etFromDate.text.toString().trim()
        val toDate = viewBinding.etToDate.text.toString().trim()
        val days = viewBinding.etDaysCount.text.toString().toDoubleOrNull() ?: 1.0
        val reason = viewBinding.etReason.text.toString().trim()

        if (reason.isEmpty()) {
            showToast("Vui lòng nhập lý do xin nghỉ phép")
            return
        }

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = "ĐANG GỬI..."

        lifecycleScope.launch {
            val req = CreateLeaveRequest(
                absenceTypeId = absenceType.id,
                fromDate = fromDate,
                toDate = toDate,
                daysCount = days,
                reason = reason
            )

            when (val res = leaveRepository.createLeaveRequest(req)) {
                is Resource.Success -> {
                    Toast.makeText(this@CreateLeaveActivity, "Gửi đơn xin nghỉ phép thành công!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = "GỬI ĐƠN XIN NGHỈ"
                    showToast(res.message ?: "Tạo đơn thất bại")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
