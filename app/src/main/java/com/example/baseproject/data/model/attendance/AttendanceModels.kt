package com.example.baseproject.data.model.attendance

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

    @SerializedName("startTime")
    val startTime: String = "",

    @SerializedName("endTime")
    val endTime: String = "",

    @SerializedName("isDefault")
    val isDefault: Boolean = false
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

    @SerializedName("employeeFullName")
    val employeeFullName: String = "",

    @SerializedName("month")
    val month: Int = 0,

    @SerializedName("year")
    val year: Int = 0,

    @SerializedName("totalStandardWorkDays")
    val totalStandardWorkDays: Double = 0.0,

    @SerializedName("actualWorkDays")
    val actualWorkDays: Double = 0.0,

    @SerializedName("totalLateMinutes")
    val totalLateMinutes: Int = 0,

    @SerializedName("totalEarlyMinutes")
    val totalEarlyMinutes: Int = 0,

    @SerializedName("totalOvertimeHours")
    val totalOvertimeHours: Double = 0.0,

    @SerializedName("totalPaidLeaveDays")
    val totalPaidLeaveDays: Double = 0.0,

    @SerializedName("totalUnpaidLeaveDays")
    val totalUnpaidLeaveDays: Double = 0.0
)

data class RegisterLateEarlyRequest(
    @SerializedName("workingDay")
    val workingDay: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("type")
    val type: String = "LATE" // LATE or EARLY
)
