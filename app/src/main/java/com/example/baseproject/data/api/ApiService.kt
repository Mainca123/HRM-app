package com.example.baseproject.data.api

import com.example.baseproject.data.model.auth.LoginRequest
import com.example.baseproject.data.model.auth.LoginResponse
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.data.model.common.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Retrofit REST API interface for HRM backend.
 */
interface ApiService {

    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @GET("api/Auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserProfile>>

    @POST("api/Auth/logout")
    suspend fun logout(): Response<ApiResponse<String>>
}
