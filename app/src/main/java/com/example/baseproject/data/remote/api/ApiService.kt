package com.example.baseproject.data.remote.api

import com.example.baseproject.data.remote.dto.attendance.ApproveLateEarlyRequestDto
import com.example.baseproject.data.remote.dto.attendance.CheckInRequest
import com.example.baseproject.data.remote.dto.attendance.CheckOutRequest
import com.example.baseproject.data.remote.dto.attendance.CreateLateEarlyRequestDto
import com.example.baseproject.data.remote.dto.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.remote.dto.attendance.RegisterLateEarlyDto
import com.example.baseproject.data.remote.dto.attendance.ShiftDto
import com.example.baseproject.data.remote.dto.attendance.TimeInTimeOutDto
import com.example.baseproject.data.remote.dto.auth.ChangePasswordRequest
import com.example.baseproject.data.remote.dto.auth.LoginRequest
import com.example.baseproject.data.remote.dto.auth.LoginResponse
import com.example.baseproject.data.remote.dto.auth.UserProfile
import com.example.baseproject.data.remote.dto.common.ApiResponse
import com.example.baseproject.data.remote.dto.common.PagedResultDto
import com.example.baseproject.data.remote.dto.dashboard.EmployeeDashboardDto
import com.example.baseproject.data.remote.dto.dashboard.HrDashboardDto
import com.example.baseproject.data.remote.dto.dashboard.ManagerDashboardDto
import com.example.baseproject.data.remote.dto.employee.EmployeeDetailDto
import com.example.baseproject.data.remote.dto.leave.AbsenceTypeDto
import com.example.baseproject.data.remote.dto.leave.CreateLeaveRequest
import com.example.baseproject.data.remote.dto.leave.LeaveBalanceDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestKeyDto
import com.example.baseproject.data.remote.dto.leave.RejectLeaveRequestDto
import com.example.baseproject.data.remote.dto.leave.UpdateLeaveRequestDto
import com.example.baseproject.data.remote.dto.overtime.CreateOvertimeRequest
import com.example.baseproject.data.remote.dto.overtime.OvertimeRequestDto
import com.example.baseproject.data.remote.dto.overtime.OvertimeTypesResponseDto
import com.example.baseproject.data.remote.dto.overtime.UpdateOvertimeRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Complete Retrofit REST API interface for HRM backend.
 * Accurately mapped to ASP.NET Core 9 Controllers.
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

    // --- Employee Profile ---
    @GET("api/employees/{id}")
    suspend fun getEmployeeById(
        @Path("id") id: Int
    ): Response<ApiResponse<EmployeeDetailDto>>

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
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null,
        @Query("departmentId") departmentId: String? = null,
        @Query("search") search: String? = null
    ): Response<ApiResponse<PagedResultDto<MonthlyAttendanceSummaryDto>>>

    @POST("api/attendance/explain-late-early")
    suspend fun explainLateEarly(
        @Body request: CreateLateEarlyRequestDto
    ): Response<ApiResponse<RegisterLateEarlyDto>>

    @GET("api/attendance/explain-late-early")
    suspend fun getLateEarlyRequests(
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("employeeId") employeeId: Int? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null
    ): Response<ApiResponse<PagedResultDto<RegisterLateEarlyDto>>>

    @DELETE("api/attendance/explain-late-early")
    suspend fun deleteLateEarlyRequest(
        @Query("employeeId") employeeId: Int,
        @Query("fromDate") fromDate: String,
        @Query("type") type: Boolean
    ): Response<ApiResponse<Boolean>>

    @PUT("api/attendance/explain-late-early/approve")
    suspend fun approveLateEarlyRequest(
        @Body request: ApproveLateEarlyRequestDto
    ): Response<ApiResponse<RegisterLateEarlyDto>>

    // --- Leave Requests ---
    @GET("api/absence-types")
    suspend fun getAbsenceTypes(): Response<ApiResponse<List<AbsenceTypeDto>>>

    @GET("api/leave-requests/balance/{employeeId}")
    suspend fun getLeaveBalance(
        @Path("employeeId") employeeId: Int
    ): Response<ApiResponse<LeaveBalanceDto>>

    @GET("api/leave-requests")
    suspend fun getLeaveRequests(
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("status") status: Byte? = null,
        @Query("employeeId") employeeId: Int? = null,
        @Query("departmentId") departmentId: String? = null
    ): Response<ApiResponse<PagedResultDto<LeaveRequestDto>>>

    @POST("api/leave-requests")
    suspend fun createLeaveRequest(
        @Body request: CreateLeaveRequest
    ): Response<ApiResponse<LeaveRequestDto>>

    @PUT("api/leave-requests")
    suspend fun updateLeaveRequest(
        @Body request: UpdateLeaveRequestDto
    ): Response<ApiResponse<LeaveRequestDto>>

    @POST("api/leave-requests/approve")
    suspend fun approveLeaveRequest(
        @Body request: LeaveRequestKeyDto
    ): Response<ApiResponse<LeaveRequestDto>>

    @POST("api/leave-requests/reject")
    suspend fun rejectLeaveRequest(
        @Body request: RejectLeaveRequestDto
    ): Response<ApiResponse<LeaveRequestDto>>

    @POST("api/leave-requests/cancel")
    suspend fun cancelLeaveRequest(
        @Body request: LeaveRequestKeyDto
    ): Response<ApiResponse<LeaveRequestDto>>

    // --- Overtime ---
    @GET("api/overtime/types")
    suspend fun getOvertimeTypes(): Response<ApiResponse<OvertimeTypesResponseDto>>

    @GET("api/overtime/requests")
    suspend fun getOvertimeRequests(
        @Query("pageIndex") pageIndex: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("employeeId") employeeId: Int? = null,
        @Query("departmentId") departmentId: String? = null
    ): Response<ApiResponse<PagedResultDto<OvertimeRequestDto>>>

    @POST("api/overtime/requests")
    suspend fun createOvertimeRequest(
        @Body request: CreateOvertimeRequest
    ): Response<ApiResponse<OvertimeRequestDto>>

    @PUT("api/overtime/requests")
    suspend fun updateOvertimeRequest(
        @Body request: UpdateOvertimeRequestDto
    ): Response<ApiResponse<OvertimeRequestDto>>

    @DELETE("api/overtime/requests")
    suspend fun deleteOvertimeRequest(
        @Query("employeeId") employeeId: Int,
        @Query("fromDate") fromDate: String,
        @Query("type") type: Byte
    ): Response<ApiResponse<Boolean>>
}
