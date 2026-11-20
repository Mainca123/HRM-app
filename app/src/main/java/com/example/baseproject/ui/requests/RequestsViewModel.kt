package com.example.baseproject.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.data.model.leave.LeaveBalanceDto
import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.example.baseproject.data.model.overtime.OvertimeRequestDto
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.LeaveRepository
import com.example.baseproject.data.repositories.OvertimeRepository
import com.example.baseproject.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository,
    private val overtimeRepository: OvertimeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestsUiState())
    val uiState: StateFlow<RequestsUiState> = _uiState.asStateFlow()

    init {
        val user = authRepository.getUserProfile()
        _uiState.value = _uiState.value.copy(currentUser = user)
        loadData()
    }

    fun loadData() {
        val user = _uiState.value.currentUser
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, feedbackMessage = null)

            // Leave balance
            user?.employeeId?.let { empId ->
                when (val balanceRes = leaveRepository.getLeaveBalance(empId)) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(leaveBalance = balanceRes.data)
                    }
                    else -> {}
                }
            }

            // Leave requests
            val leaveRes = leaveRepository.getLeaveRequests()
            val leaves = if (leaveRes is Resource.Success) leaveRes.data ?: emptyList() else emptyList()

            // Overtime requests
            val otRes = overtimeRepository.getOvertimeRequests()
            val ots = if (otRes is Resource.Success) otRes.data ?: emptyList() else emptyList()

            // Approvals (pending leaves)
            val pendingLeaves = leaves.filter { it.status.equals("PENDING", ignoreCase = true) }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                leaveRequests = leaves,
                overtimeRequests = ots,
                pendingApprovals = pendingLeaves
            )
        }
    }

    fun approveRequest(request: LeaveRequestDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = leaveRepository.approveLeave(request.id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(feedbackMessage = "Đã phê duyệt đơn của ${request.employeeFullName ?: ""}")
                    loadData()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, feedbackMessage = res.message ?: "Duyệt đơn thất bại")
                }
                else -> {}
            }
        }
    }

    fun rejectRequest(request: LeaveRequestDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = leaveRepository.rejectLeave(request.id)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(feedbackMessage = "Đã từ chối đơn của ${request.employeeFullName ?: ""}")
                    loadData()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, feedbackMessage = res.message ?: "Từ chối thất bại")
                }
                else -> {}
            }
        }
    }

    fun clearFeedback() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}

data class RequestsUiState(
    val currentUser: UserProfile? = null,
    val leaveBalance: LeaveBalanceDto? = null,
    val leaveRequests: List<LeaveRequestDto> = emptyList(),
    val overtimeRequests: List<OvertimeRequestDto> = emptyList(),
    val pendingApprovals: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null
)
