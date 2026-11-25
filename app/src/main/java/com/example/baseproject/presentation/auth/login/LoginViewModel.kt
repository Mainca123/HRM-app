package com.example.baseproject.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.remote.dto.auth.UserProfile
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        val savedUsername = authRepository.getSavedUsername()
        val isRemember = authRepository.isRememberLogin()
        _uiState.value = _uiState.value.copy(
            username = savedUsername,
            rememberLogin = isRemember
        )
    }

    fun onUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = null)
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
    }

    fun onRememberLoginChanged(remember: Boolean) {
        _uiState.value = _uiState.value.copy(rememberLogin = remember)
    }

    fun login() {
        val currentState = _uiState.value
        val username = currentState.username.trim()
        val password = currentState.password.trim()

        if (username.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng nhập tên đăng nhập hoặc mã nhân viên")
            return
        }

        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng nhập mật khẩu")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, loginSuccessUser = null)

            // Save remember state & username
            authRepository.setRememberLogin(currentState.rememberLogin)
            if (currentState.rememberLogin) {
                authRepository.setSavedUsername(username)
            } else {
                authRepository.setSavedUsername("")
            }

            when (val result = authRepository.login(username, password)) {
                is Resource.Success -> {
                    val user = result.data?.user
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loginSuccessUser = user
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "Đăng nhập thất bại"
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberLogin: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessUser: UserProfile? = null
)
