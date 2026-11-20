package com.example.baseproject.ui.forms

import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.model.overtime.CreateOvertimeRequest
import com.example.baseproject.data.model.overtime.OvertimeTypeDto
import com.example.baseproject.data.repositories.OvertimeRepository
import com.example.baseproject.databinding.ActivityCreateOvertimeBinding
import com.example.baseproject.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CreateOvertimeActivity : BaseActivity<ActivityCreateOvertimeBinding>() {

    @Inject
    lateinit var overtimeRepository: OvertimeRepository

    private var overtimeTypes: List<OvertimeTypeDto> = emptyList()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun provideViewBinding(): ActivityCreateOvertimeBinding =
        ActivityCreateOvertimeBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()

        viewBinding.etWorkDate.setText(dateFormat.format(calendar.time))
        viewBinding.etStartTime.setText("17:30")
        viewBinding.etEndTime.setText("20:30")
        viewBinding.etHours.setText("3.0")

        initListeners()
        loadOvertimeTypes()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.etWorkDate.setOnClickListener {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH)
            val d = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                viewBinding.etWorkDate.setText(dateFormat.format(calendar.time))
            }, y, m, d).show()
        }

        viewBinding.btnSubmit.setOnClickListener {
            submitOvertime()
        }
    }

    private fun loadOvertimeTypes() {
        lifecycleScope.launch {
            when (val res = overtimeRepository.getOvertimeTypes()) {
                is Resource.Success -> {
                    overtimeTypes = res.data ?: emptyList()
                    val labels = if (overtimeTypes.isNotEmpty()) {
                        overtimeTypes.map { "${it.name} (x${it.coefficient})" }
                    } else {
                        listOf("OT Ngày thường (x1.5)", "OT Ngày nghỉ (x2.0)", "OT Ngày lễ (x3.0)")
                    }
                    val adapter = ArrayAdapter(this@CreateOvertimeActivity, android.R.layout.simple_spinner_dropdown_item, labels)
                    viewBinding.spOvertimeType.adapter = adapter
                }
                else -> {
                    val fallback = listOf("OT Ngày thường (x1.5)", "OT Ngày nghỉ (x2.0)", "OT Ngày lễ (x3.0)")
                    viewBinding.spOvertimeType.adapter = ArrayAdapter(this@CreateOvertimeActivity, android.R.layout.simple_spinner_dropdown_item, fallback)
                }
            }
        }
    }

    private fun submitOvertime() {
        val selectedIndex = viewBinding.spOvertimeType.selectedItemPosition
        val otTypeId = overtimeTypes.getOrNull(selectedIndex)?.id ?: "OT1"
        val workDate = viewBinding.etWorkDate.text.toString().trim()
        val start = viewBinding.etStartTime.text.toString().trim()
        val end = viewBinding.etEndTime.text.toString().trim()
        val hours = viewBinding.etHours.text.toString().toDoubleOrNull() ?: 3.0
        val reason = viewBinding.etReason.text.toString().trim()

        if (reason.isEmpty()) {
            showToast("Vui lòng nhập nội dung công việc làm thêm")
            return
        }

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = "ĐANG GỬI..."

        lifecycleScope.launch {
            val req = CreateOvertimeRequest(
                overtimeTypeId = otTypeId,
                workDate = workDate,
                startTime = start,
                endTime = end,
                hours = hours,
                reason = reason
            )

            when (val res = overtimeRepository.createOvertimeRequest(req)) {
                is Resource.Success -> {
                    Toast.makeText(this@CreateOvertimeActivity, "Gửi đơn đăng ký OT thành công!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = "GỬI ĐĂNG KÝ LÀM THÊM GIỜ"
                    showToast(res.message ?: "Đăng ký OT thất bại")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
