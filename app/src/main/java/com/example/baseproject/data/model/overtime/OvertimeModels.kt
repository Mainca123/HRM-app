package com.example.baseproject.data.model.overtime

import com.google.gson.annotations.SerializedName

data class OvertimeTypeDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("coefficient")
    val coefficient: Double = 1.5,

    @SerializedName("description")
    val description: String? = null
)

data class OvertimeRequestDto(
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

    @SerializedName("overtimeTypeId")
    val overtimeTypeId: String = "",

    @SerializedName("overtimeTypeName")
    val overtimeTypeName: String? = null,

    @SerializedName("workDate")
    val workDate: String = "",

    @SerializedName("startTime")
    val startTime: String = "",

    @SerializedName("endTime")
    val endTime: String = "",

    @SerializedName("hours")
    val hours: Double = 0.0,

    @SerializedName("reason")
    val reason: String = "",

    @SerializedName("status")
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED

    @SerializedName("approverName")
    val approverName: String? = null
)

data class CreateOvertimeRequest(
    @SerializedName("overtimeTypeId")
    val overtimeTypeId: String,

    @SerializedName("workDate")
    val workDate: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String,

    @SerializedName("hours")
    val hours: Double,

    @SerializedName("reason")
    val reason: String
)

data class OvertimeActionRequest(
    @SerializedName("requestId")
    val requestId: Int,

    @SerializedName("reason")
    val reason: String? = null
)
