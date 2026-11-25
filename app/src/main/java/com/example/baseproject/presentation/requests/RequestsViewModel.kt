package com.example.baseproject.presentation.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.remote.dto.attendance.RegisterLateEarlyDto
import com.example.baseproject.data.remote.dto.auth.UserProfile
import com.example.baseproject.data.remote.dto.leave.LeaveBalanceDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.data.remote.dto.overtime.OvertimeRequestDto
import com.example.baseproject.data.repository.AttendanceRepository
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.data.repository.LeaveRepository
import com.example.baseproject.data.repository.OvertimeRepository
import com.example.baseproject.core.common.Resource
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
    private val attendanceRepository: AttendanceRepository,
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

            // Explanations (Late/Early)
            val explainRes = attendanceRepository.getLateEarlyRequests()
            val explains = if (explainRes is Resource.Success) explainRes.data ?: emptyList() else emptyList()

            // Approvals (pending leaves from other employees - never self-approve)
            val currentEmpId = user?.employeeId
            val pendingLeaves = leaves.filter { it.isPending && it.employeeId != currentEmpId }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                leaveRequests = leaves,
                overtimeRequests = ots,
                explanationRequests = explains,
                pendingApprovals = pendingLeaves
            )
        }
    }

    fun approveRequest(request: LeaveRequestDto) {
        if (request.employeeId == _uiState.value.currentUser?.employeeId) {
            _uiState.value = _uiState.value.copy(feedbackMessage = "Bạn không thể tự phê duyệt đơn của chính mình.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = leaveRepository.approveLeave(request.employeeId, request.fromDate, request.absenceId)) {
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
        if (request.employeeId == _uiState.value.currentUser?.employeeId) {
            _uiState.value = _uiState.value.copy(feedbackMessage = "Bạn không thể tự từ chối đơn của chính mình.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = leaveRepository.rejectLeave(request.employeeId, request.fromDate, request.absenceId, "Không phù hợp lịch công tác")) {
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

    fun cancelLeaveRequest(request: LeaveRequestDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = leaveRepository.cancelLeave(request.employeeId, request.fromDate, request.absenceId)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(feedbackMessage = "Đã hủy đơn nghỉ phép")
                    loadData()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, feedbackMessage = res.message ?: "Hủy đơn thất bại")
                }
                else -> {}
            }
        }
    }

    fun deleteOvertimeRequest(request: OvertimeRequestDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = overtimeRepository.deleteOvertimeRequest(request.employeeId, request.fromDate.take(10), request.type)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(feedbackMessage = "Đã xóa đơn đăng ký tăng ca")
                    loadData()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, feedbackMessage = res.message ?: "Xóa đơn thất bại")
                }
                else -> {}
            }
        }
    }

    fun deleteExplanationRequest(request: RegisterLateEarlyDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = attendanceRepository.deleteLateEarlyRequest(request.employeeId, request.fromDate.take(10), request.type)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(feedbackMessage = "Đã xóa đơn giải trình")
                    loadData()
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, feedbackMessage = res.message ?: "Xóa đơn thất bại")
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
    val explanationRequests: List<RegisterLateEarlyDto> = emptyList(),
    val pendingApprovals: List<LeaveRequestDto> = emptyList(),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null
)
