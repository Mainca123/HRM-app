package com.example.baseproject.data.model.leave

import com.google.gson.annotations.SerializedName

data class AbsenceTypeDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("isDeductible")
    val isDeductible: Boolean = false,

    @SerializedName("description")
    val description: String? = null
)

data class LeaveBalanceDto(
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("standardLeaveDays")
    val standardLeaveDays: Double = 12.0,

    @SerializedName("seniorityLeaveDays")
    val seniorityLeaveDays: Double = 0.0,

    @SerializedName("usedLeaveDays")
    val usedLeaveDays: Double = 0.0,

    @SerializedName("remainingLeaveDays")
    val remainingLeaveDays: Double = 12.0,

    @SerializedName("year")
    val year: Int = 2026
)

data class LeaveRequestDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("employeeFullName")
    val employeeFullName: String? = null,

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("absenceTypeId")
    val absenceTypeId: String = "",

    @SerializedName("absenceTypeName")
    val absenceTypeName: String = "",

    @SerializedName("fromDate")
    val fromDate: String = "",

    @SerializedName("toDate")
    val toDate: String = "",

    @SerializedName("daysCount")
    val daysCount: Double = 1.0,

    @SerializedName("reason")
    val reason: String = "",

    @SerializedName("status")
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, CANCELLED

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("approverName")
    val approverName: String? = null,

    @SerializedName("rejectReason")
    val rejectReason: String? = null
)

data class CreateLeaveRequest(
    @SerializedName("absenceTypeId")
    val absenceTypeId: String,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("toDate")
    val toDate: String,

    @SerializedName("daysCount")
    val daysCount: Double,

    @SerializedName("reason")
    val reason: String
)

data class LeaveActionRequest(
    @SerializedName("requestId")
    val requestId: Int,

    @SerializedName("reason")
    val reason: String? = null
)
