package com.example.baseproject.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.remote.dto.auth.UserProfile
import com.example.baseproject.data.remote.dto.dashboard.EmployeeDashboardDto
import com.example.baseproject.data.repository.AttendanceRepository
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.data.repository.DashboardRepository
import com.example.baseproject.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val attendanceRepository: AttendanceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val user = authRepository.getUserProfile()
        _uiState.value = _uiState.value.copy(currentUser = user)
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = dashboardRepository.getEmployeeDashboard()) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dashboard = result.data
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun handleCheckInOut(gpsNotes: String? = null) {
        val currentDashboard = _uiState.value.dashboard
        val hasCheckedIn = !currentDashboard?.todayTimeIn.isNullOrBlank()
        val hasCheckedOut = !currentDashboard?.todayTimeOut.isNullOrBlank()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingInOut = true, checkInOutMessage = null)

            if (!hasCheckedIn) {
                // Perform Check-in with GPS verification note
                when (val result = attendanceRepository.checkIn(notes = gpsNotes)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isCheckingInOut = false,
                            checkInOutMessage = "Vào ca thành công lúc ${result.data?.timeIn?.takeLast(8) ?: ""}"
                        )
                        loadDashboard()
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isCheckingInOut = false,
                            errorMessage = result.message ?: "Check-in thất bại"
                        )
                    }
                    is Resource.Loading -> {}
                }
            } else if (!hasCheckedOut) {
                // Perform Check-out with GPS verification note
                when (val result = attendanceRepository.checkOut(notes = gpsNotes)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isCheckingInOut = false,
                            checkInOutMessage = "Tan ca thành công lúc ${result.data?.timeOut?.takeLast(8) ?: ""}"
                        )
                        loadDashboard()
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isCheckingInOut = false,
                            errorMessage = result.message ?: "Check-out thất bại"
                        )
                    }
                    is Resource.Loading -> {}
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isCheckingInOut = false,
                    checkInOutMessage = "Bạn đã hoàn thành lượt chấm công hôm nay!"
                )
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(checkInOutMessage = null, errorMessage = null)
    }
}

data class HomeUiState(
    val currentUser: UserProfile? = null,
    val dashboard: EmployeeDashboardDto? = null,
    val isLoading: Boolean = false,
    val isCheckingInOut: Boolean = false,
    val checkInOutMessage: String? = null,
    val errorMessage: String? = null
)
