package com.example.baseproject.ui.requests

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.R
import com.example.baseproject.base.fragment.BaseFragment
import com.example.baseproject.databinding.FragmentRequestsBinding
import com.example.baseproject.ui.forms.CreateExplanationActivity
import com.example.baseproject.ui.forms.CreateLeaveActivity
import com.example.baseproject.ui.forms.CreateOvertimeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestsFragment : BaseFragment<FragmentRequestsBinding>() {

    private val viewModel: RequestsViewModel by viewModels()

    private val leaveAdapter = LeaveRequestAdapter()
    private val overtimeAdapter = OvertimeAdapter()
    private lateinit var approvalAdapter: ApprovalAdapter

    private var currentTab = 0 // 0: Leave, 1: OT, 2: Explanation, 3: Approval

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRequestsBinding = FragmentRequestsBinding.inflate(inflater, container, false)

    override fun initViews() {
        super.initViews()

        approvalAdapter = ApprovalAdapter(
            onApproveClick = { request ->
                viewModel.approveRequest(request)
            },
            onRejectClick = { request ->
                viewModel.rejectRequest(request)
            }
        )

        viewBinding.rvRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaveAdapter
        }

        initListeners()
        updateTabSelection(0)
    }

    private fun initListeners() {
        viewBinding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }

        viewBinding.tabLeave.setOnClickListener { updateTabSelection(0) }
        viewBinding.tabOvertime.setOnClickListener { updateTabSelection(1) }
        viewBinding.tabExplanation.setOnClickListener { updateTabSelection(2) }
        viewBinding.tabApproval.setOnClickListener { updateTabSelection(3) }

        viewBinding.btnCreateNew.setOnClickListener {
            when (currentTab) {
                0 -> startActivity(Intent(requireContext(), CreateLeaveActivity::class.java))
                1 -> startActivity(Intent(requireContext(), CreateOvertimeActivity::class.java))
                2 -> startActivity(Intent(requireContext(), CreateExplanationActivity::class.java))
                3 -> Toast.makeText(requireContext(), "Duyệt đơn dành cho người quản lý.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun selectSubTab(tabIndex: Int) {
        updateTabSelection(tabIndex)
    }

    private fun updateTabSelection(tabIndex: Int) {
        currentTab = tabIndex

        val tabs = listOf(
            viewBinding.tabLeave,
            viewBinding.tabOvertime,
            viewBinding.tabExplanation,
            viewBinding.tabApproval
        )

        tabs.forEachIndexed { index, textView ->
            if (index == tabIndex) {
                textView.setBackgroundResource(R.drawable.bg_btn_primary)
                textView.setTextColor(resources.getColor(R.color.white, null))
            } else {
                textView.setBackgroundResource(R.drawable.bg_btn_outline)
                textView.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        when (tabIndex) {
            0 -> {
                viewBinding.cardLeaveBalance.visibility = View.VISIBLE
                viewBinding.tvListTitle.text = "Danh sách đơn nghỉ phép"
                viewBinding.btnCreateNew.visibility = View.VISIBLE
                viewBinding.btnCreateNew.text = "+ Tạo đơn nghỉ"
                viewBinding.rvRequests.adapter = leaveAdapter
            }
            1 -> {
                viewBinding.cardLeaveBalance.visibility = View.GONE
                viewBinding.tvListTitle.text = "Danh sách đăng ký tăng ca (OT)"
                viewBinding.btnCreateNew.visibility = View.VISIBLE
                viewBinding.btnCreateNew.text = "+ Đăng ký OT"
                viewBinding.rvRequests.adapter = overtimeAdapter
            }
            2 -> {
                viewBinding.cardLeaveBalance.visibility = View.GONE
                viewBinding.tvListTitle.text = "Danh sách giải trình đi muộn / về sớm"
                viewBinding.btnCreateNew.visibility = View.VISIBLE
                viewBinding.btnCreateNew.text = "+ Giải trình"
                viewBinding.rvRequests.adapter = leaveAdapter
            }
            3 -> {
                viewBinding.cardLeaveBalance.visibility = View.GONE
                viewBinding.tvListTitle.text = "Đơn chờ phê duyệt"
                viewBinding.btnCreateNew.visibility = View.GONE
                viewBinding.rvRequests.adapter = approvalAdapter
            }
        }

        renderCurrentList()
    }

    override fun initObserver() {
        super.initObserver()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    viewBinding.swipeRefresh.isRefreshing = state.isLoading

                    state.leaveBalance?.let { b ->
                        viewBinding.tvRemainingLeaveDays.text = "${b.remainingLeaveDays} ngày"
                        viewBinding.tvStandardLeave.text = "Tiêu chuẩn: ${b.standardLeaveDays}"
                        viewBinding.tvUsedLeave.text = "Đã dùng: ${b.usedLeaveDays}"
                    }

                    leaveAdapter.submitList(state.leaveRequests)
                    overtimeAdapter.submitList(state.overtimeRequests)
                    approvalAdapter.submitList(state.pendingApprovals)

                    renderCurrentList()

                    state.feedbackMessage?.let { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearFeedback()
                    }
                }
            }
        }
    }

    private fun renderCurrentList() {
        val state = viewModel.uiState.value
        val isEmpty = when (currentTab) {
            0 -> state.leaveRequests.isEmpty()
            1 -> state.overtimeRequests.isEmpty()
            2 -> state.leaveRequests.isEmpty()
            3 -> state.pendingApprovals.isEmpty()
            else -> true
        }

        if (isEmpty && !state.isLoading) {
            viewBinding.tvEmptyState.visibility = View.VISIBLE
            viewBinding.tvEmptyState.text = when (currentTab) {
                0 -> "Chưa có đơn xin nghỉ phép nào"
                1 -> "Chưa có đơn đăng ký làm thêm giờ nào"
                2 -> "Chưa có giải trình đi muộn / về sớm"
                3 -> "Không có đơn nào đang chờ duyệt"
                else -> "Chưa có dữ liệu"
            }
        } else {
            viewBinding.tvEmptyState.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
}
