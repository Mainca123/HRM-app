package com.example.baseproject.data.remote.dto.overtime

import com.google.gson.annotations.SerializedName

data class OvertimeTypeDto(
    @SerializedName("type")
    val type: Byte = 3,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String? = null
) {
    // Helper property
    val id: String get() = type.toString()
}

data class OvertimeLimitDto(
    @SerializedName("shiftId")
    val shiftId: String? = null,

    @SerializedName("replacementShiftId")
    val replacementShiftId: String? = null,

    @SerializedName("fromDate")
    val fromDate: String? = null,

    @SerializedName("toDate")
    val toDate: String? = null,

    @SerializedName("value")
    val value: Double? = null
)

data class OvertimeTypesResponseDto(
    @SerializedName("types")
    val types: List<OvertimeTypeDto> = emptyList(),

    @SerializedName("limits")
    val limits: List<OvertimeLimitDto> = emptyList()
)

data class OvertimeRequestDto(
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
    val type: Byte = 3,

    @SerializedName("value")
    val value: Double? = 0.0,

    @SerializedName("createdUserName")
    val createdUserName: String? = null,

    @SerializedName("createdDate")
    val createdDate: String? = null,

    @SerializedName("modifiedUserName")
    val modifiedUserName: String? = null,

    @SerializedName("modifiedDate")
    val modifiedDate: String? = null
) {
    // Helper properties for UI compatibility
    val id: Int get() = "$employeeId$fromDate$type".hashCode()
    val workDate: String get() = fromDate
    val hours: Double get() = value ?: 0.0
    val isApproved: Boolean get() = !modifiedUserName.isNullOrBlank()
    val approverName: String? get() = modifiedUserName
    val overtimeTypeName: String get() = when(type.toInt()) {
        1 -> "Tăng ca trước ca"
        2 -> "Tăng ca sau ca"
        3 -> "Tăng ca theo ngày"
        else -> "Tăng ca loại $type"
    }
}

data class CreateOvertimeRequest(
    @SerializedName("employeeId")
    val employeeId: Int? = null,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("toDate")
    val toDate: String? = null,

    @SerializedName("type")
    val type: Byte = 3,

    @SerializedName("value")
    val value: Double
)

data class UpdateOvertimeRequestDto(
    @SerializedName("employeeId")
    val employeeId: Int,

    @SerializedName("fromDate")
    val fromDate: String,

    @SerializedName("type")
    val type: Byte,

    @SerializedName("toDate")
    val toDate: String? = null,

    @SerializedName("value")
    val value: Double
)
