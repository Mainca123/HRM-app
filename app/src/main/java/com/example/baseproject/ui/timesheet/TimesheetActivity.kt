package com.example.baseproject.ui.timesheet

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.repositories.AttendanceRepository
import com.example.baseproject.data.repositories.LeaveRepository
import com.example.baseproject.data.repositories.AuthRepository
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

    @Inject
    lateinit var leaveRepository: LeaveRepository

    @Inject
    lateinit var authRepository: AuthRepository

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
            // Load leave balance
            val user = authRepository.getUserProfile()
            user?.employeeId?.let { empId ->
                when (val balanceRes = leaveRepository.getLeaveBalance(empId)) {
                    is Resource.Success -> {
                        balanceRes.data?.let { b ->
                            viewBinding.tvUsedLeaveThisMonth.text = "${b.usedLeaveDays} ngày"
                            viewBinding.tvRemainingLeave.text = "${b.remainingLeaveDays} ngày"
                        }
                    }
                    else -> {}
                }
            }

            // Load monthly attendance summary
            when (val result = attendanceRepository.getMonthlySummary(selectedMonth, selectedYear)) {
                is Resource.Success -> {
                    val s = result.data
                    if (s != null) {
                        viewBinding.tvActualWorkDays.text = "${s.totalWorkDays} công"
                        viewBinding.tvStandardWorkDays.text = "22"
                        val rate = ((s.totalWorkDays / 22.0) * 100).coerceIn(0.0, 100.0).toInt()
                        viewBinding.pbWorkDaysRate.progress = rate

                        viewBinding.tvOtNormal.text = "${s.totalOvertimeNormal} giờ"
                        viewBinding.tvOtNight.text = "${s.totalOvertimeNormalNight} giờ"
                        viewBinding.tvLateMinutes.text = "${s.totalLateMinutes.toInt()} phút"
                        viewBinding.tvEarlyMinutes.text = "${s.totalEarlyMinutes.toInt()} phút"
                        viewBinding.tvMealCount.text = "${s.totalMealAllowance.toInt()} suất"
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this@TimesheetActivity, result.message ?: "Chưa có dữ liệu bảng công tháng $selectedMonth/$selectedYear", Toast.LENGTH_SHORT).show()
                    viewBinding.tvActualWorkDays.text = "0 công"
                    viewBinding.tvStandardWorkDays.text = "22"
                    viewBinding.pbWorkDaysRate.progress = 0
                    viewBinding.tvOtNormal.text = "0 giờ"
                    viewBinding.tvOtNight.text = "0 giờ"
                    viewBinding.tvLateMinutes.text = "0 phút"
                    viewBinding.tvEarlyMinutes.text = "0 phút"
                    viewBinding.tvMealCount.text = "0 suất"
                }
                else -> {}
            }
        }
    }
}
