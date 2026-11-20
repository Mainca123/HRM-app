package com.example.baseproject.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseproject.data.model.attendance.MonthlyAttendanceSummaryDto
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.data.repositories.AttendanceRepository
import com.example.baseproject.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
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
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            // Load monthly summary
            val summaryResult = attendanceRepository.getMonthlySummary(month, year)
            val summary = if (summaryResult is Resource.Success) summaryResult.data else null

            // Load daily history
            val historyResult = attendanceRepository.getMyHistory(month, year)
            val historyList = if (historyResult is Resource.Success) historyResult.data ?: emptyList() else emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                summary = summary,
                historyList = historyList,
                errorMessage = historyResult.message ?: summaryResult.message
            )
        }
    }
}

data class HistoryUiState(
    val selectedMonth: Int = 11,
    val selectedYear: Int = 2026,
    val summary: MonthlyAttendanceSummaryDto? = null,
    val historyList: List<TimeInTimeOutDto> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
