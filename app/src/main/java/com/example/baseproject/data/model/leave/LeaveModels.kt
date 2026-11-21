package com.example.baseproject.data.model.leave

import com.google.gson.annotations.SerializedName

data class AbsenceTypeDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("company")
    val company: Boolean? = true,

    @SerializedName("si")
    val si: Boolean? = false
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
    @SerializedName("employeeId")
    val employeeId: Int = 0,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("employeeFullName")
    val employeeFullName: String? = null,

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("fromDate")
    val fromDate: String = "",

    @SerializedName("toDate")
    val toDate: String? = null,

    @SerializedName("unit")
    val unit: Double? = 1.0,

    @SerializedName("absenceId")
    val absenceId: String = "",

    @SerializedName("absenceName")
    val absenceName: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("status")
    val status: Byte? = 0, // 0: Chờ duyệt, 1: Đã duyệt, 2: Từ chối, 3: Đã hủy

    @SerializedName("statusName")
    val statusName: String? = null,

    @SerializedName("createdUserName")
    val createdUserName: String? = null,

    @SerializedName("createdDate")
    val createdDate: String? = null,

    @SerializedName("modifiedUserName")
    val modifiedUserName: String? = null,

    @SerializedName("modifiedDate")
    val modifiedDate: String? = null,

    @SerializedName("approvedUserName")
    val approvedUserName: String? = null,

    @SerializedName("approvedDate")
    val approvedDate: String? = null
) {
    // Helper properties for UI compatibility
    val id: Int get() = "$employeeId$absenceId$fromDate".hashCode()
    val daysCount: Double get() = unit ?: 1.0
    val reason: String get() = notes ?: ""
    val absenceTypeName: String get() = absenceName ?: absenceId
    val approverName: String? get() = approvedUserName
    val isPending: Boolean get() = status == null || status.toInt() == 0
}

data class CreateLeaveRequest(
    @SerializedName("employeeId")
    val employeeId: Int? = null,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("toDate")
    val toDate: String,

    @SerializedName("unit")
    val unit: Double,

    @SerializedName("absenceId")
    val absenceId: String,

    @SerializedName("notes")
    val notes: String?
)

data class LeaveRequestKeyDto(
    @SerializedName("employeeId")
    val employeeId: Int,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("absenceId")
    val absenceId: String
)

data class RejectLeaveRequestDto(
    @SerializedName("employeeId")
    val employeeId: Int,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("absenceId")
    val absenceId: String,

    @SerializedName("rejectReason")
    val rejectReason: String
)
