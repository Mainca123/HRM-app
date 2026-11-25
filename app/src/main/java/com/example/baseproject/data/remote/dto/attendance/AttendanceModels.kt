package com.example.baseproject.data.remote.dto.attendance

import com.google.gson.annotations.SerializedName

data class CheckInRequest(
    @SerializedName("shiftId")
    val shiftId: String? = null,

    @SerializedName("notes")
    val notes: String? = null
)

data class CheckOutRequest(
    @SerializedName("notes")
    val notes: String? = null
)

data class ShiftDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("isDefault")
    val isDefault: Boolean? = false,

    @SerializedName("isNightShift")
    val isNightShift: Boolean? = false,

    @SerializedName("normalTimeIn")
    val normalTimeIn: String? = null,

    @SerializedName("normalTimeOut")
    val normalTimeOut: String? = null,

    @SerializedName("normalBreakOut")
    val normalBreakOut: String? = null,

    @SerializedName("normalBreakIn")
    val normalBreakIn: String? = null,

    @SerializedName("normalTotalMinutes")
    val normalTotalMinutes: Int? = null,

    @SerializedName("lateComingMinutes")
    val lateComingMinutes: Int? = null,

    @SerializedName("earlyReturningMinutes")
    val earlyReturningMinutes: Int? = null
)

data class TimeInTimeOutDto(
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("employeeFullName")
    val employeeFullName: String = "",

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("workingDay")
    val workingDay: String = "",

    @SerializedName("shiftId")
    val shiftId: String = "",

    @SerializedName("shiftName")
    val shiftName: String? = null,

    @SerializedName("timeIn")
    val timeIn: String? = null,

    @SerializedName("timeOut")
    val timeOut: String? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("lateMinutes")
    val lateMinutes: Int? = null,

    @SerializedName("earlyMinutes")
    val earlyMinutes: Int? = null,

    @SerializedName("workingHours")
    val workingHours: Double? = null
)

data class MonthlyAttendanceSummaryDto(
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("employeeFullName")
    val employeeFullName: String = "",

    @SerializedName("departmentId")
    val departmentId: String? = null,

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("month")
    val month: Int = 0,

    @SerializedName("year")
    val year: Int = 0,

    @SerializedName("totalWorkDays")
    val totalWorkDays: Double = 0.0,

    @SerializedName("totalOvertimeNormal")
    val totalOvertimeNormal: Double = 0.0,

    @SerializedName("totalOvertimeNormalNight")
    val totalOvertimeNormalNight: Double = 0.0,

    @SerializedName("totalOvertimeWeekend")
    val totalOvertimeWeekend: Double = 0.0,

    @SerializedName("totalOvertimeWeekendNight")
    val totalOvertimeWeekendNight: Double = 0.0,

    @SerializedName("totalOvertimeHoliday")
    val totalOvertimeHoliday: Double = 0.0,

    @SerializedName("totalOvertimeHolidayNight")
    val totalOvertimeHolidayNight: Double = 0.0,

    @SerializedName("totalLateMinutes")
    val totalLateMinutes: Double = 0.0,

    @SerializedName("totalEarlyMinutes")
    val totalEarlyMinutes: Double = 0.0,

    @SerializedName("totalMealAllowance")
    val totalMealAllowance: Double = 0.0
) {
    // Helper properties for UI compatibility
    val actualWorkDays: Double get() = totalWorkDays
    val totalStandardWorkDays: Double get() = 22.0
    val totalOvertimeHours: Double get() = totalOvertimeNormal + totalOvertimeNormalNight + totalOvertimeWeekend + totalOvertimeHoliday
}

data class CreateLateEarlyRequestDto(
    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("type")
    val type: Boolean, // false: Đi muộn, true: Về sớm

    @SerializedName("value")
    val value: Int = 30, // Số phút

    @SerializedName("reason")
    val reason: String
)

data class RegisterLateEarlyDto(
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("employeeFullName")
    val employeeFullName: String = "",

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("fromDate")
    val fromDate: String = "",

    @SerializedName("toDate")
    val toDate: String? = null,

    @SerializedName("type")
    val type: Boolean = false,

    @SerializedName("typeName")
    val typeName: String? = null,

    @SerializedName("value")
    val value: Int? = null,

    @SerializedName("reason")
    val reason: String? = null,

    @SerializedName("createdUserName")
    val createdUserName: String? = null,

    @SerializedName("createdDate")
    val createdDate: String? = null,

    @SerializedName("modifiedUserName")
    val modifiedUserName: String? = null,

    @SerializedName("modifiedDate")
    val modifiedDate: String? = null
)

data class ApproveLateEarlyRequestDto(
    @SerializedName("employeeId")
    val employeeId: Int,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("type")
    val type: Boolean,

    @SerializedName("adminNote")
    val adminNote: String? = null
)
