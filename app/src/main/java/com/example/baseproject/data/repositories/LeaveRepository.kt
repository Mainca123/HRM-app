package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.leave.AbsenceTypeDto
import com.example.baseproject.data.model.leave.CreateLeaveRequest
import com.example.baseproject.data.model.leave.LeaveActionRequest
import com.example.baseproject.data.model.leave.LeaveBalanceDto
import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.example.baseproject.utils.Resource
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
                Resource.Error(response.body()?.message ?: "Lỗi tải loại nghỉ phép")
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
                Resource.Error(response.body()?.message ?: "Lỗi tải số dư phép")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getLeaveRequests(status: String? = null): Resource<List<LeaveRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLeaveRequests(status)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Lỗi tải danh sách đơn nghỉ")
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
                Resource.Error(response.body()?.message ?: "Tạo đơn nghỉ phép thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun approveLeave(requestId: Int, reason: String? = null): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveLeaveRequest(LeaveActionRequest(requestId, reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()?.data ?: "Duyệt đơn thành công")
            } else {
                Resource.Error(response.body()?.message ?: "Duyệt đơn thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun rejectLeave(requestId: Int, reason: String? = null): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectLeaveRequest(LeaveActionRequest(requestId, reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()?.data ?: "Từ chối đơn thành công")
            } else {
                Resource.Error(response.body()?.message ?: "Từ chối đơn thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
