package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.attendance.ApproveLateEarlyRequestDto
import com.example.baseproject.data.model.attendance.CheckInRequest
import com.example.baseproject.data.model.attendance.CheckOutRequest
import com.example.baseproject.data.model.attendance.CreateLateEarlyRequestDto
import com.example.baseproject.data.model.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.model.attendance.RegisterLateEarlyDto
import com.example.baseproject.data.model.attendance.ShiftDto
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.utils.ApiErrorParser
import com.example.baseproject.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getShifts(): Resource<List<ShiftDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getShifts()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải ca làm việc"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun checkIn(shiftId: String? = null, notes: String? = null): Resource<TimeInTimeOutDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkIn(CheckInRequest(shiftId, notes))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Check-in thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối khi Check-in")
        }
    }

    suspend fun checkOut(notes: String? = null): Resource<TimeInTimeOutDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkOut(CheckOutRequest(notes))
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Check-out thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối khi Check-out")
        }
    }

    suspend fun getMyHistory(month: Int? = null, year: Int? = null): Resource<List<TimeInTimeOutDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMyAttendanceHistory(month, year)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải lịch sử chấm công"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getMonthlySummary(
        month: Int? = null,
        year: Int? = null,
        employeeId: Int? = null,
        search: String? = null
    ): Resource<MonthlyAttendanceSummaryDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMonthlyAttendanceSummary(
                pageIndex = 1,
                pageSize = 20,
                month = month,
                year = year,
                search = search
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val list = response.body()!!.data!!.items
                val matched = if (employeeId != null) {
                    list.firstOrNull { it.employeeId == employeeId }
                } else {
                    list.firstOrNull()
                }
                if (matched != null) {
                    Resource.Success(matched)
                } else {
                    Resource.Error("Chưa có dữ liệu bảng công tháng ${month ?: ""}/${year ?: ""}")
                }
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải bảng công tháng"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun explainLateEarly(
        fromDate: String,
        reason: String,
        isEarly: Boolean = false,
        minutes: Int = 30
    ): Resource<RegisterLateEarlyDto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateLateEarlyRequestDto(
                fromDate = fromDate,
                type = isEarly,
                value = minutes,
                reason = reason
            )
            val response = apiService.explainLateEarly(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Gửi giải trình thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getLateEarlyRequests(
        pageIndex: Int = 1,
        pageSize: Int = 50
    ): Resource<List<RegisterLateEarlyDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLateEarlyRequests(pageIndex = pageIndex, pageSize = pageSize)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.items)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải danh sách giải trình"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun deleteLateEarlyRequest(
        employeeId: Int,
        fromDate: String,
        type: Boolean
    ): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteLateEarlyRequest(employeeId, fromDate, type)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(true)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Xóa đơn giải trình thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun approveLateEarlyRequest(
        employeeId: Int,
        fromDate: String,
        type: Boolean,
        adminNote: String? = null
    ): Resource<RegisterLateEarlyDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveLateEarlyRequest(
                ApproveLateEarlyRequestDto(employeeId, fromDate, type, adminNote)
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Phê duyệt đơn giải trình thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
