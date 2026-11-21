package com.example.baseproject.ui.forms

import android.app.DatePickerDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.model.overtime.CreateOvertimeRequest
import com.example.baseproject.data.model.overtime.OvertimeTypeDto
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.DashboardRepository
import com.example.baseproject.data.repositories.OvertimeRepository
import com.example.baseproject.databinding.ActivityCreateOvertimeBinding
import com.example.baseproject.ui.requests.OvertimeAdapter
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

    override fun provideViewBinding(): ActivityCreateOvertimeBinding =
        ActivityCreateOvertimeBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

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
            submitOvertime()
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
                        selectedTypeIndex = 0
                        selectedType = overtimeTypes[0]
                        viewBinding.tvSelectedOvertimeType.text = "OT${selectedType?.type} - ${selectedType?.name}"
                    }
                }
                else -> {}
            }
        }
    }

    private fun submitOvertime() {
        val type = selectedType?.type ?: 1.toByte()
        val workDateApi = dateFormatApi.format(calendar.time)
        val hours = hoursCount

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = "ĐANG GỬI..."

        lifecycleScope.launch {
            val req = CreateOvertimeRequest(
                fromDate = workDateApi,
                toDate = workDateApi,
                type = type,
                value = hours
            )

            when (val res = overtimeRepository.createOvertimeRequest(req)) {
                is Resource.Success -> {
                    Toast.makeText(this@CreateOvertimeActivity, "Gửi đơn đăng ký OT thành công!", Toast.LENGTH_LONG).show()
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = "GỬI ĐĂNG KÝ TĂNG CA"
                    loadData()
                }
                is Resource.Error -> {
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = "GỬI ĐĂNG KÝ TĂNG CA"
                    showToast(res.message ?: "Đăng ký OT thất bại")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
