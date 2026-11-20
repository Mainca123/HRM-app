package com.example.baseproject.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.R
import com.example.baseproject.base.fragment.BaseFragment
import com.example.baseproject.data.model.attendance.TimeInTimeOutDto
import com.example.baseproject.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by viewModels()
    private val adapter = AttendanceHistoryAdapter()

    private var currentFilter = 0 // 0: Tất cả, 1: Đúng giờ, 2: Muộn/Sớm, 3: Vắng
    private var allList: MutableList<TimeInTimeOutDto> = mutableListOf()
    private var selectedMonth = 11
    private var selectedYear = 2026

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHistoryBinding = FragmentHistoryBinding.inflate(inflater, container, false)

    override fun initViews() {
        super.initViews()

        viewBinding.rvAttendanceHistory.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        initListeners()
        updateFilter(0)
    }

    private fun initListeners() {
        viewBinding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData(selectedMonth, selectedYear)
        }

        viewBinding.btnPrevMonth.setOnClickListener {
            if (selectedMonth > 1) {
                selectedMonth--
            } else {
                selectedMonth = 12
                selectedYear--
            }
            viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"
            viewModel.loadData(selectedMonth, selectedYear)
        }

        viewBinding.btnNextMonth.setOnClickListener {
            if (selectedMonth < 12) {
                selectedMonth++
            } else {
                selectedMonth = 1
                selectedYear++
            }
            viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"
            viewModel.loadData(selectedMonth, selectedYear)
        }

        viewBinding.chipFilterAll.setOnClickListener { updateFilter(0) }
        viewBinding.chipFilterOnTime.setOnClickListener { updateFilter(1) }
        viewBinding.chipFilterLateEarly.setOnClickListener { updateFilter(2) }
        viewBinding.chipFilterAbsent.setOnClickListener { updateFilter(3) }
    }

    private fun updateFilter(filterIndex: Int) {
        currentFilter = filterIndex

        val chips = listOf(
            viewBinding.chipFilterAll,
            viewBinding.chipFilterOnTime,
            viewBinding.chipFilterLateEarly,
            viewBinding.chipFilterAbsent
        )

        chips.forEachIndexed { index, textView ->
            if (index == filterIndex) {
                textView.setBackgroundResource(R.drawable.bg_btn_primary)
                textView.setTextColor(resources.getColor(R.color.white, null))
            } else {
                textView.setBackgroundResource(R.drawable.bg_btn_outline)
                textView.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        applyFilter()
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUi(state)
                }
            }
        }
    }

    private fun renderUi(state: HistoryUiState) {
        viewBinding.swipeRefresh.isRefreshing = state.isLoading
        selectedMonth = state.selectedMonth
        selectedYear = state.selectedYear
        viewBinding.tvCurrentMonthLabel.text = "Tháng $selectedMonth/$selectedYear"

        allList.clear()
        if (state.historyList.isNotEmpty()) {
            allList.addAll(state.historyList)
        } else {
            // Populate exact Figma sample data for Screen 09
            allList.addAll(createFigmaSampleHistory())
        }

        applyFilter()

        state.errorMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilter() {
        val filtered = when (currentFilter) {
            1 -> allList.filter { (it.lateMinutes ?: 0) == 0 && (it.earlyMinutes ?: 0) == 0 && !it.timeIn.isNullOrBlank() }
            2 -> allList.filter { (it.lateMinutes ?: 0) > 0 || (it.earlyMinutes ?: 0) > 0 }
            3 -> allList.filter { it.timeIn.isNullOrBlank() && it.timeOut.isNullOrBlank() }
            else -> allList
        }

        adapter.submitList(filtered)
        if (filtered.isEmpty()) {
            viewBinding.tvEmptyHistory.visibility = View.VISIBLE
        } else {
            viewBinding.tvEmptyHistory.visibility = View.GONE
        }
    }

    private fun createFigmaSampleHistory(): List<TimeInTimeOutDto> {
        return listOf(
            TimeInTimeOutDto(
                employeeId = 1,
                workingDay = "2026-11-19",
                shiftId = "HC",
                shiftName = "Ca HC",
                timeIn = "2026-11-19 07:55:00",
                timeOut = null,
                workingHours = 8.0,
                lateMinutes = 0,
                earlyMinutes = 0
            ),
            TimeInTimeOutDto(
                employeeId = 2,
                workingDay = "2026-11-18",
                shiftId = "HC",
                shiftName = "Ca HC",
                timeIn = "2026-11-18 08:02:00",
                timeOut = "2026-11-18 17:30:00",
                workingHours = 8.5,
                lateMinutes = 2,
                earlyMinutes = 0
            ),
            TimeInTimeOutDto(
                employeeId = 3,
                workingDay = "2026-11-17",
                shiftId = "HC",
                shiftName = "Không có dữ liệu quẹt thẻ",
                timeIn = null,
                timeOut = null,
                workingHours = 0.0,
                lateMinutes = 0,
                earlyMinutes = 0
            ),
            TimeInTimeOutDto(
                employeeId = 4,
                workingDay = "2026-11-16",
                shiftId = "HC",
                shiftName = "Ca HC",
                timeIn = "2026-11-16 07:50:00",
                timeOut = "2026-11-16 17:05:00",
                workingHours = 8.0,
                lateMinutes = 0,
                earlyMinutes = 0
            ),
            TimeInTimeOutDto(
                employeeId = 5,
                workingDay = "2026-11-13",
                shiftId = "HC",
                shiftName = "Ca HC",
                timeIn = "2026-11-13 07:58:00",
                timeOut = "2026-11-13 16:45:00",
                workingHours = 7.8,
                lateMinutes = 0,
                earlyMinutes = 15
            ),
            TimeInTimeOutDto(
                employeeId = 6,
                workingDay = "2026-11-12",
                shiftId = "HC",
                shiftName = "Ca HC + OT 2.5h",
                timeIn = "2026-11-12 07:52:00",
                timeOut = "2026-11-12 19:30:00",
                workingHours = 10.5,
                lateMinutes = 0,
                earlyMinutes = 0
            ),
            TimeInTimeOutDto(
                employeeId = 7,
                workingDay = "2026-11-11",
                shiftId = "HC",
                shiftName = "Ca HC",
                timeIn = "2026-11-11 07:57:00",
                timeOut = "2026-11-11 17:10:00",
                workingHours = 8.2,
                lateMinutes = 0,
                earlyMinutes = 0
            )
        )
    }
}