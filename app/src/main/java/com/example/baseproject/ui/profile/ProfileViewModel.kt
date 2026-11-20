package com.example.baseproject.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = authRepository.getUserProfile()
        _uiState.value = _uiState.value.copy(userProfile = user)

        viewModelScope.launch {
            when (val res = authRepository.getCurrentUser()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(userProfile = res.data)
                }
                else -> {}
            }
        }
    }

    fun changePassword(currentPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            when (val res = authRepository.changePassword(currentPass, newPass)) {
                is Resource.Success -> onResult(true, "Đổi mật khẩu thành công!")
                is Resource.Error -> onResult(false, res.message ?: "Đổi mật khẩu thất bại")
                is Resource.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }
}

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isLoggedOut: Boolean = false
)
