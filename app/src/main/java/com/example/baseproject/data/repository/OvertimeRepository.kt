package com.example.baseproject.data.repository

import com.example.baseproject.data.remote.api.ApiService
import com.example.baseproject.data.remote.dto.overtime.CreateOvertimeRequest
import com.example.baseproject.data.remote.dto.overtime.OvertimeRequestDto
import com.example.baseproject.data.remote.dto.overtime.OvertimeTypeDto
import com.example.baseproject.data.remote.dto.overtime.UpdateOvertimeRequestDto
import com.example.baseproject.core.common.ApiErrorParser
import com.example.baseproject.core.common.Resource
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
                Resource.Success(response.body()!!.data!!.types)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải danh mục tăng ca"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getOvertimeRequests(
        employeeId: Int? = null,
        departmentId: String? = null
    ): Resource<List<OvertimeRequestDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOvertimeRequests(
                pageIndex = 1,
                pageSize = 100,
                employeeId = employeeId,
                departmentId = departmentId
            )
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val items = response.body()!!.data!!.items
                Resource.Success(items)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải danh sách tăng ca"))
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
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Đăng ký tăng ca thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun updateOvertimeRequest(request: UpdateOvertimeRequestDto): Resource<OvertimeRequestDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateOvertimeRequest(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Cập nhật tăng ca thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun deleteOvertimeRequest(employeeId: Int, fromDate: String, type: Byte): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteOvertimeRequest(employeeId, fromDate, type)
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(true)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Xóa đăng ký tăng ca thất bại"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
