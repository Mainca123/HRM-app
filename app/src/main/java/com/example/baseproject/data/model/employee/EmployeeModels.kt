package com.example.baseproject.data.model.employee

import com.google.gson.annotations.SerializedName

data class EmployeeDetailDto(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("fullName")
    val fullName: String = "",

    @SerializedName("alias")
    val alias: String? = null,

    @SerializedName("birthDate")
    val birthDate: String? = null,

    @SerializedName("sex")
    val sex: Boolean? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("departmentId")
    val departmentId: String? = null,

    @SerializedName("departmentName")
    val departmentName: String? = null,

    @SerializedName("positionId")
    val positionId: String? = null,

    @SerializedName("positionName")
    val positionName: String? = null,

    @SerializedName("currentDepartmentFromDate")
    val currentDepartmentFromDate: String? = null,

    @SerializedName("userName")
    val userName: String? = null
)
