package com.example.baseproject.ui.forms

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.model.leave.AbsenceTypeDto
import com.example.baseproject.data.model.leave.CreateLeaveRequest
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.LeaveRepository
import com.example.baseproject.databinding.ActivityCreateLeaveBinding
import com.example.baseproject.utils.Resource
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class LeaveSession {
    ALL_DAY,
    MORNING,
    AFTERNOON
}

@AndroidEntryPoint
class CreateLeaveActivity : BaseActivity<ActivityCreateLeaveBinding>() {

    @Inject
    lateinit var leaveRepository: LeaveRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private var absenceTypes: List<AbsenceTypeDto> = emptyList()
    private var selectedAbsenceIndex: Int = 0
    private var selectedAbsenceType: AbsenceTypeDto? = null

    private val fromCalendar = Calendar.getInstance()
    private val toCalendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private var currentSession: LeaveSession = LeaveSession.ALL_DAY
    private var daysCount: Double = 1.0

    override fun provideViewBinding(): ActivityCreateLeaveBinding =
        ActivityCreateLeaveBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        // Khởi tạo ngày hiện tại
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        fromCalendar.time = today.time
        toCalendar.time = today.time

        updateDateFields()
        selectSession(LeaveSession.ALL_DAY)

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        // Chọn Loại nghỉ phép (Dropdown selector có mũi tên sổ xuống / sổ lên)
        viewBinding.layoutAbsenceType.setOnClickListener {
            showAbsenceTypePicker()
        }

        // Chọn "Từ ngày"
        viewBinding.etFromDate.setOnClickListener {
            showDatePicker(fromCalendar) {
                if (fromCalendar.after(toCalendar)) {
                    toCalendar.time = fromCalendar.time
                }
                updateDateFields()
                recalculateDays()
            }
        }

        // Chọn "Đến ngày"
        viewBinding.etToDate.setOnClickListener {
            showDatePicker(toCalendar) {
                if (toCalendar.before(fromCalendar)) {
                    fromCalendar.time = toCalendar.time
                }
                updateDateFields()
                recalculateDays()
            }
        }

        // 3 nút chọn buổi nghỉ: Cả ngày, Buổi sáng, Buổi chiều
        viewBinding.btnAllDay.setOnClickListener {
            selectSession(LeaveSession.ALL_DAY)
        }

        viewBinding.btnMorning.setOnClickListener {
            selectSession(LeaveSession.MORNING)
        }

        viewBinding.btnAfternoon.setOnClickListener {
            selectSession(LeaveSession.AFTERNOON)
        }

        viewBinding.btnSubmit.setOnClickListener {
            submitLeave()
        }
    }

    private fun selectSession(session: LeaveSession) {
        currentSession = session

        val primaryColor = ContextCompat.getColor(this, R.color.primary_blue)
        val whiteColor = ContextCompat.getColor(this, R.color.white)
        val grayText = Color.parseColor("#64748B")
        val borderColor = Color.parseColor("#CBD5E1")
        val strokeWidthPx = (1 * resources.displayMetrics.density).toInt()

        fun applyStyle(btn: MaterialButton, isSelected: Boolean) {
            if (isSelected) {
                btn.backgroundTintList = ColorStateList.valueOf(primaryColor)
                btn.setTextColor(whiteColor)
                btn.strokeWidth = 0
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(whiteColor)
                btn.setTextColor(grayText)
                btn.strokeColor = ColorStateList.valueOf(borderColor)
                btn.strokeWidth = strokeWidthPx
            }
        }

        applyStyle(viewBinding.btnAllDay, session == LeaveSession.ALL_DAY)
        applyStyle(viewBinding.btnMorning, session == LeaveSession.MORNING)
        applyStyle(viewBinding.btnAfternoon, session == LeaveSession.AFTERNOON)

        recalculateDays()
    }

    private fun recalculateDays() {
        val span = getCalendarDaysSpan()
        val multiplier = when (currentSession) {
            LeaveSession.ALL_DAY -> 1.0
            LeaveSession.MORNING, LeaveSession.AFTERNOON -> 0.5
        }
        daysCount = span * multiplier

        val formatted = if (daysCount % 1.0 == 0.0) {
            "${daysCount.toInt()}.0"
        } else {
            String.format(Locale.US, "%.1f", daysCount)
        }

        val suffix = when (currentSession) {
            LeaveSession.ALL_DAY -> "ngày"
            LeaveSession.MORNING -> "ngày (Nghỉ sáng)"
            LeaveSession.AFTERNOON -> "ngày (Nghỉ chiều)"
        }
        viewBinding.tvDaysCount.text = "$formatted $suffix"
    }

    private fun showAbsenceTypePicker() {
        if (absenceTypes.isEmpty()) {
            showToast("Đang tải danh mục loại phép, vui lòng thử lại...")
            return
        }

        // Mũi tên xoay 180 độ lên trên (sổ lên) khi mở menu
        viewBinding.ivAbsenceTypeArrow.animate().rotation(180f).setDuration(200).start()

        val names = absenceTypes.map { it.name }
        val adapter = ArrayAdapter(this, R.layout.item_dropdown_menu, names)

        val listPopupWindow = ListPopupWindow(this).apply {
            anchorView = viewBinding.layoutAbsenceType
            setAdapter(adapter)
            width = viewBinding.layoutAbsenceType.width
            val maxH = (260 * resources.displayMetrics.density).toInt()
            height = minOf((names.size * 48 * resources.displayMetrics.density).toInt() + 16, maxH)
            isModal = true
            setBackgroundDrawable(ContextCompat.getDrawable(this@CreateLeaveActivity, R.drawable.bg_dropdown_popup))

            setOnItemClickListener { _, _, position, _ ->
                selectedAbsenceIndex = position
                selectedAbsenceType = absenceTypes[position]
                viewBinding.tvSelectedAbsenceType.text = selectedAbsenceType?.name
                dismiss()
            }

            setOnDismissListener {
                // Mũi tên xoay trở lại 0 độ (sổ xuống) khi đóng menu
                viewBinding.ivAbsenceTypeArrow.animate().rotation(0f).setDuration(200).start()
            }
        }

        listPopupWindow.show()
    }

    private fun getCalendarDaysSpan(): Int {
        val c1 = Calendar.getInstance().apply {
            time = fromCalendar.time
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val c2 = Calendar.getInstance().apply {
            time = toCalendar.time
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = c2.timeInMillis - c1.timeInMillis
        return ((diffMillis / (1000 * 60 * 60 * 24)) + 1).toInt().coerceAtLeast(1)
    }

    private fun updateDateFields() {
        viewBinding.etFromDate.setText(dateFormat.format(fromCalendar.time))
        viewBinding.etToDate.setText(dateFormat.format(toCalendar.time))
    }

    private fun showDatePicker(targetCalendar: Calendar, onDateSelected: () -> Unit) {
        val y = targetCalendar.get(Calendar.YEAR)
        val m = targetCalendar.get(Calendar.MONTH)
        val d = targetCalendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, month, day ->
            targetCalendar.set(Calendar.YEAR, year)
            targetCalendar.set(Calendar.MONTH, month)
            targetCalendar.set(Calendar.DAY_OF_MONTH, day)
            targetCalendar.set(Calendar.HOUR_OF_DAY, 0)
            targetCalendar.set(Calendar.MINUTE, 0)
            targetCalendar.set(Calendar.SECOND, 0)
            targetCalendar.set(Calendar.MILLISECOND, 0)
            onDateSelected()
        }, y, m, d).show()
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load absence types
            when (val res = leaveRepository.getAbsenceTypes()) {
                is Resource.Success -> {
                    absenceTypes = res.data ?: emptyList()
                    if (absenceTypes.isNotEmpty()) {
                        selectedAbsenceIndex = 0
                        selectedAbsenceType = absenceTypes[0]
                        viewBinding.tvSelectedAbsenceType.text = selectedAbsenceType?.name
                    }
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

    private fun submitLeave() {
        val absenceType = selectedAbsenceType ?: absenceTypes.firstOrNull()
        if (absenceType == null) {
            showToast("Vui lòng chọn loại nghỉ")
            return
        }

        val fromDate = dateFormat.format(fromCalendar.time)
        val toDate = dateFormat.format(toCalendar.time)
        val days = daysCount
        val rawNotes = viewBinding.etReason.text.toString().trim()

        val finalNotes = when (currentSession) {
            LeaveSession.MORNING -> if (rawNotes.isNotEmpty()) "[Nghỉ sáng] $rawNotes" else "[Nghỉ sáng]"
            LeaveSession.AFTERNOON -> if (rawNotes.isNotEmpty()) "[Nghỉ chiều] $rawNotes" else "[Nghỉ chiều]"
            LeaveSession.ALL_DAY -> if (rawNotes.isNotEmpty()) rawNotes else null
        }

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = "ĐANG GỬI..."

        lifecycleScope.launch {
            val req = CreateLeaveRequest(
                absenceId = absenceType.id,
                fromDate = fromDate,
                toDate = toDate,
                unit = days,
                notes = finalNotes
            )

            when (val res = leaveRepository.createLeaveRequest(req)) {
                is Resource.Success -> {
                    Toast.makeText(this@CreateLeaveActivity, "Gửi đơn xin nghỉ thành công!", Toast.LENGTH_LONG).show()
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
