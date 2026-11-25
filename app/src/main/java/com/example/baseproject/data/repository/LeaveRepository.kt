package com.example.baseproject.data.repository

import com.example.baseproject.data.remote.api.ApiService
import com.example.baseproject.data.remote.dto.leave.AbsenceTypeDto
import com.example.baseproject.data.remote.dto.leave.CreateLeaveRequest
import com.example.baseproject.data.remote.dto.leave.LeaveBalanceDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestKeyDto
import com.example.baseproject.data.remote.dto.leave.RejectLeaveRequestDto
import com.example.baseproject.data.remote.dto.leave.UpdateLeaveRequestDto
import com.example.baseproject.core.common.ApiErrorParser
import com.example.baseproject.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaveRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getAbsenceTypes(): Resource<List<AbsenceTypeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAbsenceTypes()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải loại nghỉ phép"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getLeaveBalance(employeeId: Int): Resource<LeaveBalanceDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLeaveBalance(employeeId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải số dư phép"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getLeaveRequests(
        status: Byte? = null,
        employeeId: Int? = null,
        departmentId: String? = null
    ): Resource<List<LeaveRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLeaveRequests(
                pageIndex = 1,
                pageSize = 100,
                status = status,
                employeeId = employeeId,
                departmentId = departmentId
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val items = response.body()!!.data!!.items
                Resource.Success(items)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải danh sách đơn nghỉ"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun createLeaveRequest(request: CreateLeaveRequest): Resource<LeaveRequestDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createLeaveRequest(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Tạo đơn nghỉ thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun updateLeaveRequest(request: UpdateLeaveRequestDto): Resource<LeaveRequestDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateLeaveRequest(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Cập nhật đơn nghỉ thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun approveLeave(
        employeeId: Int,
        fromDate: String,
        absenceId: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val key = LeaveRequestKeyDto(employeeId, fromDate, absenceId)
            val response = apiService.approveLeaveRequest(key)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success("Phê duyệt đơn nghỉ phép thành công")
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Duyệt đơn thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun rejectLeave(
        employeeId: Int,
        fromDate: String,
        absenceId: String,
        reason: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val rejectReq = RejectLeaveRequestDto(
                employeeId = employeeId,
                fromDate = fromDate,
                absenceId = absenceId,
                rejectReason = reason
            )
            val response = apiService.rejectLeaveRequest(rejectReq)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success("Từ chối đơn thành công")
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Từ chối đơn thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun cancelLeave(
        employeeId: Int,
        fromDate: String,
        absenceId: String
    ): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val key = LeaveRequestKeyDto(employeeId, fromDate, absenceId)
            val response = apiService.cancelLeaveRequest(key)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success("Hủy đơn thành công")
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Hủy đơn thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
