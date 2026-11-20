package com.example.baseproject.data.repositories

import com.example.baseproject.data.api.ApiService
import com.example.baseproject.data.model.auth.LoginRequest
import com.example.baseproject.data.model.auth.LoginResponse
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.utils.Resource
import com.example.baseproject.utils.SpManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val spManager: SpManager
) {

    suspend fun login(username: String, password: String): Resource<LoginResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username = username, password = password))
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success && apiResponse.data != null) {
                        val loginData = apiResponse.data
                        spManager.saveAccessToken(loginData.token)
                        spManager.saveUserProfile(loginData.user)
                        spManager.setLoggedIn(true)
                        Resource.Success(loginData)
                    } else {
                        val errorMsg = apiResponse?.message ?: "Đăng nhập thất bại"
                        Resource.Error(errorMsg)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val msg = if (!errorBody.isNullOrBlank() && errorBody.contains("message")) {
                        try {
                            val jsonObj = com.google.gson.JsonParser.parseString(errorBody).asJsonObject
                            jsonObj.get("message")?.asString ?: "Tên đăng nhập hoặc mật khẩu không chính xác"
                        } catch (_: Exception) {
                            "Tên đăng nhập hoặc mật khẩu không chính xác"
                        }
                    } else {
                        "Đăng nhập thất bại (Mã lỗi ${response.code()})"
                    }
                    Resource.Error(msg)
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.")
            }
        }

    suspend fun getCurrentUser(): Resource<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                val user = response.body()!!.data!!
                spManager.saveUserProfile(user)
                Resource.Success(user)
            } else {
                Resource.Error(response.body()?.message ?: "Không lấy được thông tin người dùng")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }

    suspend fun logout(): Resource<String> = withContext(Dispatchers.IO) {
        try {
            apiService.logout()
        } catch (_: Exception) {
            // Ignore API logout error if token expired
        }
        spManager.logout()
        Resource.Success("Đăng xuất thành công")
    }

    fun isRememberLogin(): Boolean = spManager.isRememberLogin()

    fun setRememberLogin(remember: Boolean) = spManager.setRememberLogin(remember)

    fun getSavedUsername(): String = spManager.getSavedUsername()

    fun setSavedUsername(username: String) = spManager.setSavedUsername(username)

    fun getUserProfile(): UserProfile? = spManager.getUserProfile()

    fun isLoggedIn(): Boolean = spManager.isLoggedIn()

    suspend fun changePassword(currentPass: String, newPass: String): Resource<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.changePassword(com.example.baseproject.data.model.auth.ChangePasswordRequest(currentPass, newPass))
            if (response.isSuccessful && response.body()?.success == true) {
                Resource.Success(true)
            } else {
                Resource.Error(response.body()?.message ?: "Đổi mật khẩu thất bại")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Lỗi kết nối máy chủ")
        }
    }
}
