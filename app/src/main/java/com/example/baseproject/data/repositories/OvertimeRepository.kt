package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.overtime.CreateOvertimeRequest
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.data.model.overtime.OvertimeTypeDto
import com.example.baseproject.data.model.overtime.UpdateOvertimeRequestDto
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
                Resource.Success(response.body()!!.data!!.types)
            } else {
                Resource.Error(response.body()?.message ?: "Lỗi tải danh mục tăng ca")
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

    suspend fun updateOvertimeRequest(request: UpdateOvertimeRequestDto): Resource<OvertimeRequestDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateOvertimeRequest(request)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(response.body()?.message ?: "Cập nhật tăng ca thất bại")
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
                Resource.Error(response.body()?.message ?: "Xóa đăng ký tăng ca thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
