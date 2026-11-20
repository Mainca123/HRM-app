package com.example.baseproject.data.model.common

import com.google.gson.annotations.SerializedName

/**
 * Standard API Response envelope matching ASP.NET Core backend.
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("errors")
    val errors: List<String>? = null
)
