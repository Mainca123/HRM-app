package com.example.baseproject.data.model.dashboard

import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.google.gson.annotations.SerializedName

data class EmployeeDashboardDto(
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeFullName")
    val employeeFullName: String = "",

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("positionName")
    val positionName: String? = null,

    @SerializedName("currentMonth")
    val currentMonth: Int = 0,

    @SerializedName("currentYear")
    val currentYear: Int = 0,

    @SerializedName("workDaysThisMonth")
    val workDaysThisMonth: Double = 0.0,

    @SerializedName("overtimeHoursThisMonth")
    val overtimeHoursThisMonth: Double = 0.0,

    @SerializedName("annualLeaveBalance")
    val annualLeaveBalance: Double = 0.0,

    @SerializedName("lateMinutesThisMonth")
    val lateMinutesThisMonth: Int = 0,

    @SerializedName("todayTimeIn")
    val todayTimeIn: String? = null,

    @SerializedName("todayTimeOut")
    val todayTimeOut: String? = null,

    @SerializedName("todayShiftName")
    val todayShiftName: String? = null,

    @SerializedName("recentLeaveRequests")
    val recentLeaveRequests: List<LeaveRequestDto> = emptyList()
)

data class ManagerDashboardDto(
    @SerializedName("departmentId")
    val departmentId: String? = null,

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("totalEmployees")
    val totalEmployees: Int = 0,

    @SerializedName("presentTodayCount")
    val presentTodayCount: Int = 0,

    @SerializedName("absentTodayCount")
    val absentTodayCount: Int = 0,

    @SerializedName("lateTodayCount")
    val lateTodayCount: Int = 0,

    @SerializedName("pendingLeaveRequestsCount")
    val pendingLeaveRequestsCount: Int = 0,

    @SerializedName("pendingExplanationRequestsCount")
    val pendingExplanationRequestsCount: Int = 0
)

data class HrDashboardDto(
    @SerializedName("totalCompanyEmployees")
    val totalCompanyEmployees: Int = 0,

    @SerializedName("companyPresentToday")
    val companyPresentToday: Int = 0,

    @SerializedName("companyAbsentToday")
    val companyAbsentToday: Int = 0,

    @SerializedName("companyLateToday")
    val companyLateToday: Int = 0,

    @SerializedName("totalDepartments")
    val totalDepartments: Int = 0,

    @SerializedName("pendingApprovalsCount")
    val pendingApprovalsCount: Int = 0
)
