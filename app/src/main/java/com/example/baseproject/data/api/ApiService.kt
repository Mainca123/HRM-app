package com.example.baseproject.data.api

import com.example.baseproject.data.model.attendance.CheckInRequest
import com.example.baseproject.data.model.attendance.CheckOutRequest
import com.example.baseproject.data.model.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.model.attendance.RegisterLateEarlyRequest
import com.example.baseproject.data.model.attendance.ShiftDto
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.data.model.auth.ChangePasswordRequest
import com.example.baseproject.data.model.auth.LoginRequest
import com.example.baseproject.data.model.auth.LoginResponse
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.data.model.common.ApiResponse
import com.example.baseproject.data.model.dashboard.EmployeeDashboardDto
import com.example.baseproject.data.model.dashboard.HrDashboardDto
import com.example.baseproject.data.model.dashboard.ManagerDashboardDto
import com.example.baseproject.data.model.leave.AbsenceTypeDto
import com.example.baseproject.data.model.leave.CreateLeaveRequest
import com.example.baseproject.data.model.leave.LeaveActionRequest
import com.example.baseproject.data.model.leave.LeaveBalanceDto
import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.example.baseproject.data.model.overtime.CreateOvertimeRequest
import com.example.baseproject.data.model.overtime.OvertimeActionRequest
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.data.model.overtime.OvertimeTypeDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Complete Retrofit REST API interface for HRM backend.
 */
interface ApiService {

    // --- Authentication ---
    @POST("api/Auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @GET("api/Auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserProfile>>

    @POST("api/Auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ApiResponse<Boolean>>

    @POST("api/Auth/logout")
    suspend fun logout(): Response<ApiResponse<String>>

    // --- Dashboard ---
    @GET("api/dashboard/employee")
    suspend fun getEmployeeDashboard(): Response<ApiResponse<EmployeeDashboardDto>>

    @GET("api/dashboard/manager")
    suspend fun getManagerDashboard(
        @Query("departmentId") departmentId: String? = null
    ): Response<ApiResponse<ManagerDashboardDto>>

    @GET("api/dashboard/hr")
    suspend fun getHrDashboard(): Response<ApiResponse<HrDashboardDto>>

    // --- Attendance & Check-in ---
    @GET("api/attendance/shifts")
    suspend fun getShifts(): Response<ApiResponse<List<ShiftDto>>>

    @POST("api/attendance/check-in")
    suspend fun checkIn(
        @Body request: CheckInRequest
    ): Response<ApiResponse<TimeInTimeOutDto>>

    @POST("api/attendance/check-out")
    suspend fun checkOut(
        @Body request: CheckOutRequest
    ): Response<ApiResponse<TimeInTimeOutDto>>

    @GET("api/attendance/my-history")
    suspend fun getMyAttendanceHistory(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<List<TimeInTimeOutDto>>>

    @GET("api/attendance/monthly-summary")
    suspend fun getMonthlyAttendanceSummary(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<MonthlyAttendanceSummaryDto>>

    @POST("api/attendance/explain-late-early")
    suspend fun explainLateEarly(
        @Body request: RegisterLateEarlyRequest
    ): Response<ApiResponse<String>>

    // --- Leave Requests ---
    @GET("api/absence-types")
    suspend fun getAbsenceTypes(): Response<ApiResponse<List<AbsenceTypeDto>>>

    @GET("api/leave-requests/balance/{employeeId}")
    suspend fun getLeaveBalance(
        @Path("employeeId") employeeId: Int
    ): Response<ApiResponse<LeaveBalanceDto>>

    @GET("api/leave-requests")
    suspend fun getLeaveRequests(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<LeaveRequestDto>>>

    @POST("api/leave-requests")
    suspend fun createLeaveRequest(
        @Body request: CreateLeaveRequest
    ): Response<ApiResponse<LeaveRequestDto>>

    @POST("api/leave-requests/approve")
    suspend fun approveLeaveRequest(
        @Body request: LeaveActionRequest
    ): Response<ApiResponse<String>>

    @POST("api/leave-requests/reject")
    suspend fun rejectLeaveRequest(
        @Body request: LeaveActionRequest
    ): Response<ApiResponse<String>>

    // --- Overtime ---
    @GET("api/overtime/types")
    suspend fun getOvertimeTypes(): Response<ApiResponse<List<OvertimeTypeDto>>>

    @GET("api/overtime/requests")
    suspend fun getOvertimeRequests(
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<OvertimeRequestDto>>>

    @POST("api/overtime/requests")
    suspend fun createOvertimeRequest(
        @Body request: CreateOvertimeRequest
    ): Response<ApiResponse<OvertimeRequestDto>>

    @POST("api/overtime/requests/approve")
    suspend fun approveOvertimeRequest(
        @Body request: OvertimeActionRequest
    ): Response<ApiResponse<String>>

    @POST("api/overtime/requests/reject")
    suspend fun rejectOvertimeRequest(
        @Body request: OvertimeActionRequest
    ): Response<ApiResponse<String>>
}
