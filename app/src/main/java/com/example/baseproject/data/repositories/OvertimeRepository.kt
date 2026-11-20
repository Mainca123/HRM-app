package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.overtime.CreateOvertimeRequest
import com.example.baseproject.data.model.overtime.OvertimeActionRequest
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.data.model.overtime.OvertimeTypeDto
import com.example.baseproject.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OvertimeRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getOvertimeTypes(): Resource<List<OvertimeTypeDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOvertimeTypes()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Lỗi tải loại tăng ca")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getOvertimeRequests(status: String? = null): Resource<List<OvertimeRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOvertimeRequests(status)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Lỗi tải danh sách tăng ca")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun createOvertimeRequest(request: CreateOvertimeRequest): Resource<OvertimeRequestDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createOvertimeRequest(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Đăng ký tăng ca thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun approveOvertime(requestId: Int, reason: String? = null): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.approveOvertimeRequest(OvertimeActionRequest(requestId, reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()?.data ?: "Duyệt tăng ca thành công")
            } else {
                Resource.Error(response.body()?.message ?: "Duyệt tăng ca thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun rejectOvertime(requestId: Int, reason: String? = null): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.rejectOvertimeRequest(OvertimeActionRequest(requestId, reason))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(response.body()?.data ?: "Từ chối tăng ca thành công")
            } else {
                Resource.Error(response.body()?.message ?: "Từ chối tăng ca thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
