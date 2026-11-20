package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.attendance.CheckInRequest
import com.example.baseproject.data.model.attendance.CheckOutRequest
import com.example.baseproject.data.model.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.model.attendance.RegisterLateEarlyRequest
import com.example.baseproject.data.model.attendance.ShiftDto
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
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
                Resource.Error(response.body()?.message ?: "Lỗi tải ca làm việc")
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
                Resource.Error(response.body()?.message ?: "Check-in thất bại")
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
                Resource.Error(response.body()?.message ?: "Check-out thất bại")
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
                Resource.Error(response.body()?.message ?: "Lỗi tải lịch sử chấm công")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getMonthlySummary(month: Int? = null, year: Int? = null): Resource<MonthlyAttendanceSummaryDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMonthlyAttendanceSummary(month, year)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Lỗi tải bảng công tháng")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun explainLateEarly(workingDay: String, reason: String, type: String): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.explainLateEarly(RegisterLateEarlyRequest(workingDay, reason, type))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()?.data ?: "Gửi giải trình thành công")
            } else {
                Resource.Error(response.body()?.message ?: "Gửi giải trình thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
