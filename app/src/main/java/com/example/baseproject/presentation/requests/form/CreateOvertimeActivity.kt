package com.example.baseproject.presentation.requests.form

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.R
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.data.remote.dto.overtime.CreateOvertimeRequest
import com.example.baseproject.data.remote.dto.overtime.OvertimeTypeDto
import com.example.baseproject.data.remote.dto.overtime.UpdateOvertimeRequestDto
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.data.repository.DashboardRepository
import com.example.baseproject.data.repository.OvertimeRepository
import com.example.baseproject.databinding.ActivityCreateOvertimeBinding
import com.example.baseproject.presentation.requests.adapter.OvertimeAdapter
import com.example.baseproject.core.helper.DialogTone
import com.example.baseproject.core.helper.ModernDialogHelper
import com.example.baseproject.core.common.Resource
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

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private var overtimeTypes: List<OvertimeTypeDto> = emptyList()
    private var selectedTypeIndex = 0
    private var selectedType: OvertimeTypeDto? = null

    private val calendar = Calendar.getInstance()
    private val dateFormatDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val dateFormatApi = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var hoursCount: Double = 3.0

    private var isEditMode: Boolean = false
    private var editEmployeeId: Int = 0
    private var editFromDateRaw: String = ""
    private var editType: Byte = 1

    companion object {
        const val EXTRA_IS_EDIT = "extra_is_edit"
        const val EXTRA_EMPLOYEE_ID = "extra_employee_id"
        const val EXTRA_FROM_DATE = "extra_from_date"
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_HOURS = "extra_hours"
    }

    override fun provideViewBinding(): ActivityCreateOvertimeBinding =
        ActivityCreateOvertimeBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT, false)
        if (isEditMode) {
            editEmployeeId = intent.getIntExtra(EXTRA_EMPLOYEE_ID, 0)
            editFromDateRaw = intent.getStringExtra(EXTRA_FROM_DATE).orEmpty().take(10)
            editType = intent.getByteExtra(EXTRA_TYPE, 1)
            hoursCount = intent.getDoubleExtra(EXTRA_HOURS, 3.0).coerceIn(0.5, 12.0)

            try {
                if (editFromDateRaw.isNotBlank()) {
                    calendar.time = dateFormatApi.parse(editFromDateRaw) ?: calendar.time
                }
            } catch (_: Exception) {}

            viewBinding.layoutWorkDate.isEnabled = false
            viewBinding.layoutWorkDate.alpha = 0.6f
            viewBinding.layoutOvertimeType.isEnabled = false
            viewBinding.layoutOvertimeType.alpha = 0.6f
            viewBinding.btnSubmit.text = "CẬP NHẬT TĂNG CA"
        }

        // Khởi tạo giá trị ngày làm thêm và số giờ
        viewBinding.tvWorkDate.text = dateFormatDisplay.format(calendar.time)
        updateHoursDisplay()

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        // Chọn ngày làm thêm
        viewBinding.layoutWorkDate.setOnClickListener {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH)
            val d = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                viewBinding.tvWorkDate.text = dateFormatDisplay.format(calendar.time)
            }, y, m, d).show()
        }

        // Chọn Ca làm thêm (Dropdown selector có mũi tên xoay sổ lên / sổ xuống)
        viewBinding.layoutOvertimeType.setOnClickListener {
            showOvertimeTypePicker()
        }

        // Giảm (-) số giờ làm thêm (bước nhảy 0.5 giờ)
        viewBinding.btnMinusHours.setOnClickListener {
            if (hoursCount <= 0.5) {
                Toast.makeText(this, "Số giờ làm thêm tối thiểu là 0.5 giờ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            hoursCount -= 0.5
            updateHoursDisplay()
        }

        // Tăng (+) số giờ làm thêm (bước nhảy 0.5 giờ)
        viewBinding.btnPlusHours.setOnClickListener {
            if (hoursCount >= 12.0) {
                Toast.makeText(this, "Số giờ làm thêm tối đa là 12.0 giờ / ngày", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            hoursCount += 0.5
            updateHoursDisplay()
        }

        viewBinding.btnSubmit.setOnClickListener {
            confirmAndSubmitOvertime()
        }
    }

    private fun updateHoursDisplay() {
        val formatted = if (hoursCount % 1.0 == 0.0) {
            "${hoursCount.toInt()}.0"
        } else {
            String.format(Locale.US, "%.1f", hoursCount)
        }
        viewBinding.tvHoursCount.text = "$formatted giờ"
    }

    private fun showOvertimeTypePicker() {
        if (overtimeTypes.isEmpty()) {
            showToast("Đang tải danh mục ca làm thêm, vui lòng thử lại...")
            return
        }

        // Xoay mũi tên 180 độ lên trên khi mở menu
        viewBinding.ivOvertimeTypeArrow.animate().rotation(180f).setDuration(200).start()

        val labels = overtimeTypes.map { "OT${it.type} - ${it.name}" }
        val adapter = ArrayAdapter(this, R.layout.item_dropdown_menu, labels)

        val anchorWidth = viewBinding.layoutOvertimeType.width
        val desiredWidth = maxOf(anchorWidth, (280 * resources.displayMetrics.density).toInt())
        val offset = -(desiredWidth - anchorWidth)

        val listPopupWindow = ListPopupWindow(this).apply {
            anchorView = viewBinding.layoutOvertimeType
            setAdapter(adapter)
            width = desiredWidth
            horizontalOffset = offset
            val maxH = (260 * resources.displayMetrics.density).toInt()
            height = minOf((labels.size * 48 * resources.displayMetrics.density).toInt() + 16, maxH)
            isModal = true
            setBackgroundDrawable(ContextCompat.getDrawable(this@CreateOvertimeActivity, R.drawable.bg_dropdown_popup))

            setOnItemClickListener { _, _, position, _ ->
                selectedTypeIndex = position
                selectedType = overtimeTypes[position]
                viewBinding.tvSelectedOvertimeType.text = "OT${selectedType?.type} - ${selectedType?.name}"
                dismiss()
            }

            setOnDismissListener {
                // Xoay mũi tên trở lại 0 độ hướng xuống khi đóng menu
                viewBinding.ivOvertimeTypeArrow.animate().rotation(0f).setDuration(200).start()
            }
        }

        listPopupWindow.show()
    }

    private fun loadData() {
        lifecycleScope.launch {
            // 1. Tải tiến độ số giờ OT tháng này từ Employee Dashboard
            when (val dashRes = dashboardRepository.getEmployeeDashboard()) {
                is Resource.Success -> {
                    val otHours = dashRes.data?.overtimeHoursThisMonth ?: 0.0
                    viewBinding.tvMonthlyOtHours.text = "$otHours / 40 giờ"
                }
                else -> {
                    viewBinding.tvMonthlyOtHours.text = "0.0 / 40 giờ"
                }
            }

            // 2. Tải danh mục loại tăng ca từ Backend
            when (val typesRes = overtimeRepository.getOvertimeTypes()) {
                is Resource.Success -> {
                    overtimeTypes = typesRes.data ?: emptyList()
                    if (overtimeTypes.isNotEmpty()) {
                        val matchedIdx = if (isEditMode) {
                            overtimeTypes.indexOfFirst { it.type == editType }.takeIf { it >= 0 } ?: 0
                        } else 0
                        selectedTypeIndex = matchedIdx
                        selectedType = overtimeTypes[matchedIdx]
                        viewBinding.tvSelectedOvertimeType.text = "OT${selectedType?.type} - ${selectedType?.name}"
                    }
                }
                else -> {}
            }
        }
    }

    private fun confirmAndSubmitOvertime() {
        val typeName = selectedType?.let { "OT${it.type} - ${it.name}" } ?: "Tăng ca"
        val workDateDisplay = dateFormatDisplay.format(calendar.time)
        val title = if (isEditMode) "Xác nhận cập nhật tăng ca" else "Xác nhận đăng ký tăng ca"
        val msg = if (isEditMode) {
            "Bạn có chắc chắn muốn cập nhật đơn $typeName ngày $workDateDisplay thành $hoursCount giờ?"
        } else {
            "Bạn có chắc chắn muốn đăng ký $typeName ngày $workDateDisplay ($hoursCount giờ)?"
        }
        ModernDialogHelper.showConfirm(
            context = this,
            title = title,
            message = msg,
            positiveText = getString(R.string.confirm),
            negativeText = getString(R.string.cancel),
            tone = DialogTone.PRIMARY,
            iconRes = R.drawable.ic_ot,
            onPositive = { submitOvertime() }
        )
    }

    private fun submitOvertime() {
        val type = selectedType?.type ?: 1.toByte()
        val workDateApi = dateFormatApi.format(calendar.time)
        val hours = hoursCount

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = if (isEditMode) "ĐANG CẬP NHẬT..." else "ĐANG GỬI..."

        lifecycleScope.launch {
            val res = if (isEditMode) {
                val req = UpdateOvertimeRequestDto(
                    employeeId = editEmployeeId,
                    fromDate = editFromDateRaw,
                    type = editType,
                    value = hours
                )
                overtimeRepository.updateOvertimeRequest(req)
            } else {
                val req = CreateOvertimeRequest(
                    fromDate = workDateApi,
                    toDate = workDateApi,
                    type = type,
                    value = hours
                )
                overtimeRepository.createOvertimeRequest(req)
            }

            when (res) {
                is Resource.Success -> {
                    val msg = if (isEditMode) "Cập nhật đơn OT thành công!" else "Gửi đơn đăng ký OT thành công!"
                    Toast.makeText(this@CreateOvertimeActivity, msg, Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = if (isEditMode) "CẬP NHẬT TĂNG CA" else "GỬI ĐĂNG KÝ TĂNG CA"
                    showToast(res.message ?: if (isEditMode) "Cập nhật OT thất bại" else "Đăng ký OT thất bại")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
