package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.leave.AbsenceTypeDto
import com.example.baseproject.data.model.leave.CreateLeaveRequest
import com.example.baseproject.data.model.leave.LeaveBalanceDto
import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.example.baseproject.data.model.leave.LeaveRequestKeyDto
import com.example.baseproject.data.model.leave.RejectLeaveRequestDto
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
                val errorBody = response.errorBody()?.string()
                val msg = if (!errorBody.isNullOrBlank() && errorBody.contains("message")) {
                    try {
                        val jsonObj = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                        jsonObj.get("message")?.asString ?: "Tạo đơn nghỉ thất bại"
                    } catch (_: Exception) {
                        "Tạo đơn nghỉ thất bại"
                    }
                } else {
                    response.body()?.message ?: "Tạo đơn nghỉ thất bại"
                }
                Resource.Error(msg)
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
                Resource.Error(response.body()?.message ?: "Duyệt đơn thất bại")
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
                Resource.Error(response.body()?.message ?: "Từ chối đơn thất bại")
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
                Resource.Error(response.body()?.message ?: "Hủy đơn thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
