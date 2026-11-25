package com.example.baseproject.presentation.attendance.timesheet

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.data.repository.AttendanceRepository
import com.example.baseproject.data.repository.LeaveRepository
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.databinding.ActivityTimesheetBinding
import com.example.baseproject.core.common.Resource
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

            // Load monthly attendance summary (use employeeCode search for managers; fallback to my-history for employees)
            when (val result = attendanceRepository.getMonthlySummary(
                month = selectedMonth,
                year = selectedYear,
                employeeId = user?.employeeId,
                search = user?.employeeCode
            )) {
                is Resource.Success -> {
                    val s = result.data
                    if (s != null) {
                        bindSummary(
                            workDays = s.totalWorkDays,
                            otNormal = s.totalOvertimeNormal,
                            otNight = s.totalOvertimeNormalNight,
                            lateMinutes = s.totalLateMinutes.toInt(),
                            earlyMinutes = s.totalEarlyMinutes.toInt(),
                            mealCount = s.totalMealAllowance.toInt()
                        )
                    }
                }
                is Resource.Error -> {
                    // Employee role does not have access to /api/attendance/monthly-summary (403); compute from personal history
                    when (val historyRes = attendanceRepository.getMyHistory(selectedMonth, selectedYear)) {
                        is Resource.Success -> {
                            val records = historyRes.data.orEmpty()
                            val workDays = records.count { !it.timeIn.isNullOrBlank() && !it.timeOut.isNullOrBlank() }.toDouble()
                            val lateMinutes = records.sumOf { it.lateMinutes ?: 0 }
                            val earlyMinutes = records.sumOf { it.earlyMinutes ?: 0 }
                            bindSummary(
                                workDays = workDays,
                                otNormal = 0.0,
                                otNight = 0.0,
                                lateMinutes = lateMinutes,
                                earlyMinutes = earlyMinutes,
                                mealCount = 0
                            )
                        }
                        else -> {
                            Toast.makeText(
                                this@TimesheetActivity,
                                result.message ?: "Chưa có dữ liệu bảng công tháng $selectedMonth/$selectedYear",
                                Toast.LENGTH_SHORT
                            ).show()
                            bindSummary(0.0, 0.0, 0.0, 0, 0, 0)
                        }
                    }
                }
                else -> {}
            }
        }
    }

    private fun bindSummary(
        workDays: Double,
        otNormal: Double,
        otNight: Double,
        lateMinutes: Int,
        earlyMinutes: Int,
        mealCount: Int
    ) {
        viewBinding.tvActualWorkDays.text = "$workDays công"
        viewBinding.tvStandardWorkDays.text = "22"
        val rate = ((workDays / 22.0) * 100).coerceIn(0.0, 100.0).toInt()
        viewBinding.pbWorkDaysRate.progress = rate

        viewBinding.tvOtNormal.text = "$otNormal giờ"
        viewBinding.tvOtNight.text = "$otNight giờ"
        viewBinding.tvLateMinutes.text = "$lateMinutes phút"
        viewBinding.tvEarlyMinutes.text = "$earlyMinutes phút"
        viewBinding.tvMealCount.text = "$mealCount suất"
    }
}
