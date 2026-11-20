package com.example.baseproject.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.base.fragment.BaseFragment
import com.example.baseproject.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>() {

    private val viewModel: HistoryViewModel by viewModels()
    private val adapter = AttendanceHistoryAdapter()

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

        viewBinding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
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
        viewBinding.tvCurrentMonthLabel.text = "Tháng ${state.selectedMonth}/${state.selectedYear}"

        state.summary?.let { s ->
            viewBinding.tvActualWorkDays.text = "${s.actualWorkDays}"
            viewBinding.tvStandardWorkDays.text = "/ ${s.totalStandardWorkDays} công chuẩn"
            viewBinding.tvTotalOT.text = "${s.totalOvertimeHours} giờ"
            viewBinding.tvTotalLate.text = "${s.totalLateMinutes} phút"
            viewBinding.tvTotalLeave.text = "${s.totalPaidLeaveDays} ngày"
        }

        adapter.submitList(state.historyList)

        if (state.historyList.isEmpty() && !state.isLoading) {
            viewBinding.tvEmptyHistory.visibility = View.VISIBLE
        } else {
            viewBinding.tvEmptyHistory.visibility = View.GONE
        }

        state.errorMessage?.let {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }
}
