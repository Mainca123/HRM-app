package com.example.baseproject.data.remote.dto.dashboard

import com.example.baseproject.data.remote.dto.attendance.RegisterLateEarlyDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
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

    @SerializedName("reportDate")
    val reportDate: String? = null,

    @SerializedName("totalDepartmentEmployees")
    val totalDepartmentEmployees: Int = 0,

    @SerializedName("presentToday")
    val presentToday: Int = 0,

    @SerializedName("absentToday")
    val absentToday: Int = 0,

    @SerializedName("attendanceRate")
    val attendanceRate: Double = 0.0,

    @SerializedName("pendingLeaveRequestsCount")
    val pendingLeaveRequestsCount: Int = 0,

    @SerializedName("pendingLateEarlyRequestsCount")
    val pendingLateEarlyRequestsCount: Int = 0,

    @SerializedName("pendingLeaves")
    val pendingLeaves: List<LeaveRequestDto> = emptyList(),

    @SerializedName("pendingLateEarlies")
    val pendingLateEarlies: List<RegisterLateEarlyDto> = emptyList()
) {
    // Backward compatibility helpers
    val totalEmployees: Int get() = totalDepartmentEmployees
    val presentTodayCount: Int get() = presentToday
    val absentTodayCount: Int get() = absentToday
    val pendingExplanationRequestsCount: Int get() = pendingLateEarlyRequestsCount
}

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
