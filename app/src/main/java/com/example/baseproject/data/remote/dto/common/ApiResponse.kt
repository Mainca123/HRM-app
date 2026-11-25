package com.example.baseproject.data.remote.dto.common

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

/**
 * Standard Paged result envelope matching ASP.NET Core backend PagedResultDto<T>.
 */
data class PagedResultDto<T>(
    @SerializedName("items")
    val items: List<T> = emptyList(),

    @SerializedName("totalCount")
    val totalCount: Int = 0,

    @SerializedName("pageIndex")
    val pageIndex: Int = 1,

    @SerializedName("pageSize")
    val pageSize: Int = 10,

    @SerializedName("totalPages")
    val totalPages: Int = 0
)
