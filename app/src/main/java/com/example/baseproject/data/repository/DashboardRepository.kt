package com.example.baseproject.data.repository

import com.example.baseproject.data.remote.api.ApiService
import com.example.baseproject.data.remote.dto.dashboard.EmployeeDashboardDto
import com.example.baseproject.data.remote.dto.dashboard.HrDashboardDto
import com.example.baseproject.data.remote.dto.dashboard.ManagerDashboardDto
import com.example.baseproject.core.common.ApiErrorParser
import com.example.baseproject.core.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getEmployeeDashboard(): Resource<EmployeeDashboardDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getEmployeeDashboard()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Không tải được dữ liệu bảng điều khiển"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun getManagerDashboard(departmentId: String? = null): Resource<ManagerDashboardDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getManagerDashboard(departmentId)
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải dữ liệu quản lý"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối")
        }
    }

    suspend fun getHrDashboard(): Resource<HrDashboardDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHrDashboard()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                Resource.Error(ApiErrorParser.extractErrorMessage(response, "Lỗi tải dữ liệu HR"))
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối")
        }
    }
}
