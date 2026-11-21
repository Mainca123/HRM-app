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
            var m = selectedMonth
            var y = selectedYear
            if (m > 1) {
                m--
            } else {
                m = 12
                y--
            }
            viewModel.loadData(m, y)
        }

        viewBinding.btnNextMonth.setOnClickListener {
            var m = selectedMonth
            var y = selectedYear
            if (m < 12) {
                m++
            } else {
                m = 1
                y++
            }
            viewModel.loadData(m, y)
        }

        viewBinding.tvCurrentMonthLabel.setOnClickListener {
            showMonthYearPickerDialog()
        }

        viewBinding.chipFilterAll.setOnClickListener { updateFilter(0) }
        viewBinding.chipFilterOnTime.setOnClickListener { updateFilter(1) }
        viewBinding.chipFilterLateEarly.setOnClickListener { updateFilter(2) }
        viewBinding.chipFilterAbsent.setOnClickListener { updateFilter(3) }
    }

    private fun showMonthYearPickerDialog() {
        val context = requireContext()
        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            setPadding(48, 24, 48, 16)
        }

        val monthPicker = android.widget.NumberPicker(context).apply {
            minValue = 1
            maxValue = 12
            value = selectedMonth
            displayedValues = (1..12).map { "Tháng $it" }.toTypedArray()
        }

        val yearPicker = android.widget.NumberPicker(context).apply {
            minValue = 2020
            maxValue = 2035
            value = selectedYear
        }

        layout.addView(monthPicker, android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        layout.addView(yearPicker, android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
            .setTitle("Chọn tháng / năm")
            .setView(layout)
            .setPositiveButton("Xác nhận") { _, _ ->
                viewModel.loadData(monthPicker.value, yearPicker.value)
            }
            .setNegativeButton("Hủy", null)
            .show()
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
            viewBinding.tvEmptyHistory.text = "Không có dữ liệu chấm công tháng $selectedMonth/$selectedYear"
            viewBinding.tvEmptyHistory.visibility = View.VISIBLE
        } else {
            viewBinding.tvEmptyHistory.visibility = View.GONE
        }
    }
}