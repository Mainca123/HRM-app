package com.example.baseproject.ui.timesheet

import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.repositories.AttendanceRepository
import com.example.baseproject.databinding.ActivityTimesheetBinding
import com.example.baseproject.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class TimesheetActivity : BaseActivity<ActivityTimesheetBinding>() {

    @Inject
    lateinit var attendanceRepository: AttendanceRepository

    private var selectedMonth = 11
    private var selectedYear = 2026

    override fun provideViewBinding(): ActivityTimesheetBinding =
        ActivityTimesheetBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        val cal = Calendar.getInstance()
        selectedMonth = cal.get(Calendar.MONTH) + 1
        selectedYear = cal.get(Calendar.YEAR)
        viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.btnPrevMonth.setOnClickListener {
            if (selectedMonth > 1) {
                selectedMonth--
            } else {
                selectedMonth = 12
                selectedYear--
            }
            viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"
            loadData()
        }

        viewBinding.btnNextMonth.setOnClickListener {
            if (selectedMonth < 12) {
                selectedMonth++
            } else {
                selectedMonth = 1
                selectedYear++
            }
            viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"
            loadData()
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            val result = attendanceRepository.getMonthlySummary(selectedMonth, selectedYear)
            if (result is Resource.Success && result.data != null) {
                val s = result.data
                viewBinding.tvActualWorkDays.text = "${s.actualWorkDays} công"
                viewBinding.tvStandardWorkDays.text = "${s.totalStandardWorkDays}"
                val rate = if (s.totalStandardWorkDays > 0) ((s.actualWorkDays / s.totalStandardWorkDays) * 100).toInt() else 98
                viewBinding.pbWorkDaysRate.progress = rate
                viewBinding.tvOtNormal.text = "${s.totalOvertimeHours} giờ"
                viewBinding.tvLateMinutes.text = "${s.totalLateMinutes} phút"
            } else {
                // Populate default Figma sample data (14 · Bảng công tháng)
                viewBinding.tvActualWorkDays.text = "21.5 công"
                viewBinding.tvStandardWorkDays.text = "22"
                viewBinding.pbWorkDaysRate.progress = 98
                viewBinding.tvOtNormal.text = "10.0 giờ"
                viewBinding.tvOtNight.text = "4.0 giờ"
                viewBinding.tvLateMinutes.text = "25 phút"
                viewBinding.tvEarlyMinutes.text = "0 phút"
                viewBinding.tvMealCount.text = "21 suất"
                viewBinding.tvUsedLeaveThisMonth.text = "0.5 ngày"
                viewBinding.tvRemainingLeave.text = "11.0 ngày"
            }
        }
    }
}