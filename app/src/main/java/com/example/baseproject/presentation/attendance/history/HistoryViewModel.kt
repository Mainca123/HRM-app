package com.example.baseproject.presentation.attendance.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.remote.dto.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.remote.dto.attendance.TimeInTimeOutDto
import com.example.baseproject.data.repository.AttendanceRepository
import com.example.baseproject.core.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.baseproject.data.repository.DashboardRepository
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        _uiState.value = _uiState.value.copy(selectedMonth = currentMonth, selectedYear = currentYear)
        loadData(currentMonth, currentYear)
    }

    fun loadData(month: Int = _uiState.value.selectedMonth, year: Int = _uiState.value.selectedYear) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedMonth = month,
                selectedYear = year,
                isLoading = true,
                errorMessage = null
            )

            val combinedList = mutableListOf<TimeInTimeOutDto>()

            // 1. Tải lịch sử chấm công từ Backend
            val historyResult = attendanceRepository.getMyHistory(month, year)
            if (historyResult is Resource.Success && !historyResult.data.isNullOrEmpty()) {
                combinedList.addAll(historyResult.data)
            }

            // 2. Nếu đang xem tháng hiện tại và chưa có bản ghi ngày hôm nay trong historyList, lấy thêm từ Employee Dashboard
            val cal = Calendar.getInstance()
            val isCurrentMonth = (month == (cal.get(Calendar.MONTH) + 1)) && (year == cal.get(Calendar.YEAR))
            if (isCurrentMonth) {
                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(cal.time)
                val hasTodayInHistory = combinedList.any { it.workingDay.startsWith(todayStr) }
                if (!hasTodayInHistory) {
                    when (val dashRes = dashboardRepository.getEmployeeDashboard()) {
                        is Resource.Success -> {
                            dashRes.data?.let { d ->
                                if (!d.todayTimeIn.isNullOrBlank()) {
                                    val todayItem = TimeInTimeOutDto(
                                        employeeId = d.employeeId,
                                        employeeFullName = d.employeeFullName,
                                        departmentName = d.departmentName,
                                        workingDay = "${todayStr}T00:00:00",
                                        shiftId = "HC",
                                        shiftName = d.todayShiftName ?: "Ca HC",
                                        timeIn = d.todayTimeIn,
                                        timeOut = d.todayTimeOut,
                                        workingHours = if (d.todayTimeIn != null && d.todayTimeOut != null) 8.0 else null,
                                        lateMinutes = 0,
                                        earlyMinutes = 0
                                    )
                                    combinedList.add(0, todayItem)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            _uiState.value = _uiState.value.copy(
                selectedMonth = month,
                selectedYear = year,
                isLoading = false,
                historyList = combinedList,
                errorMessage = null
            )
        }
    }
}

data class HistoryUiState(
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val summary: MonthlyAttendanceSummaryDto? = null,
    val historyList: List<TimeInTimeOutDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
