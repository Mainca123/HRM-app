package com.example.baseproject.data.model.auth

import com.google.gson.annotations.SerializedName

/**
 * Request payload for login endpoint /api/Auth/login
 */
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

/**
 * Response payload from /api/Auth/login
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String = "",

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("expiresAt")
    val expiresAt: String = "",

    @SerializedName("user")
    val user: UserProfile
)

/**
 * User and employee profile information returned by Auth endpoints
 */
data class UserProfile(
    @SerializedName("username")
    val username: String = "",

    @SerializedName("employeeId")
    val employeeId: Int? = null,

    @SerializedName("employeeCode")
    val employeeCode: String? = null,

    @SerializedName("fullName")
    val fullName: String = "",

    @SerializedName("role")
    val role: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword")
    val currentPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)
