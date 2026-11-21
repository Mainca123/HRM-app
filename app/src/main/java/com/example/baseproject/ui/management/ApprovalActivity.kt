package com.example.baseproject.ui.management

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.model.leave.LeaveRequestDto
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.LeaveRepository
import com.example.baseproject.databinding.ActivityApprovalBinding
import com.example.baseproject.ui.requests.ApprovalAdapter
import com.example.baseproject.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApprovalActivity : BaseActivity<ActivityApprovalBinding>() {

    @Inject
    lateinit var leaveRepository: LeaveRepository

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var approvalAdapter: ApprovalAdapter
    private var allRequests: MutableList<LeaveRequestDto> = mutableListOf()
    private var currentFilter = 0 // 0: Leave, 1: OT, 2: Explanation

    override fun provideViewBinding(): ActivityApprovalBinding =
        ActivityApprovalBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        val dept = intent.getStringExtra("DEPT_NAME") ?: "Phòng Kỹ thuật"
        viewBinding.tvDeptHeader.text = dept

        approvalAdapter = ApprovalAdapter(
            onApproveClick = { request ->
                handleApprove(request)
            },
            onRejectClick = { request ->
                handleReject(request)
            }
        )

        viewBinding.rvPendingApprovals.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@ApprovalActivity)
            adapter = approvalAdapter
        }

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        viewBinding.tabFilterLeave.setOnClickListener { updateFilter(0) }
        viewBinding.tabFilterOvertime.setOnClickListener { updateFilter(1) }
        viewBinding.tabFilterExplain.setOnClickListener { updateFilter(2) }
    }

    private fun updateFilter(filterIndex: Int) {
        currentFilter = filterIndex

        val tabs = listOf(
            viewBinding.tabFilterLeave,
            viewBinding.tabFilterOvertime,
            viewBinding.tabFilterExplain
        )

        tabs.forEachIndexed { index, textView ->
            if (index == filterIndex) {
                textView.setBackgroundResource(R.drawable.bg_btn_primary)
                textView.setTextColor(resources.getColor(R.color.white, null))
            } else {
                textView.setBackgroundResource(R.drawable.bg_btn_outline)
                textView.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }

        filterList()
    }

    private fun loadData() {
        viewBinding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            val result = leaveRepository.getLeaveRequests()
            viewBinding.swipeRefresh.isRefreshing = false

            val list = when (result) {
                is Resource.Success -> result.data?.filter { it.isPending } ?: emptyList()
                else -> {
                    Toast.makeText(this@ApprovalActivity, result.message ?: "Không tải được danh sách đơn", Toast.LENGTH_SHORT).show()
                    emptyList()
                }
            }

            allRequests.clear()
            allRequests.addAll(list)
            filterList()
        }
    }

    private fun filterList() {
        val filtered = when (currentFilter) {
            0 -> allRequests.filter { it.absenceTypeName.contains("Nghỉ", ignoreCase = true) || it.absenceTypeName.isBlank() }
            1 -> allRequests.filter { it.absenceTypeName.contains("tăng ca", ignoreCase = true) || it.absenceTypeName.contains("OT", ignoreCase = true) }
            else -> allRequests.filter { it.absenceTypeName.contains("giải trình", ignoreCase = true) }
        }

        if (filtered.isEmpty()) {
            viewBinding.rvPendingApprovals.visibility = View.GONE
            viewBinding.layoutEmptyApproval.visibility = View.VISIBLE
        } else {
            viewBinding.rvPendingApprovals.visibility = View.VISIBLE
            viewBinding.layoutEmptyApproval.visibility = View.GONE
            approvalAdapter.submitList(filtered.toList())
        }
    }

    private fun handleApprove(request: LeaveRequestDto) {
        lifecycleScope.launch {
            val result = leaveRepository.approveLeave(request.employeeId, request.fromDate, request.absenceId)
            when (result) {
                is Resource.Success<*> -> {
                    Toast.makeText(this@ApprovalActivity, "Đã duyệt đơn của ${request.employeeFullName ?: "nhân viên"}!", Toast.LENGTH_SHORT).show()
                    allRequests.removeAll { it.employeeId == request.employeeId && it.fromDate == request.fromDate && it.absenceId == request.absenceId }
                    filterList()
                }
                is Resource.Error<*> -> {
                    Toast.makeText(this@ApprovalActivity, result.message ?: "Phê duyệt thất bại", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun handleReject(request: LeaveRequestDto) {
        lifecycleScope.launch {
            val result = leaveRepository.rejectLeave(
                request.employeeId,
                request.fromDate,
                request.absenceId,
                "Không phù hợp kế hoạch công tác"
            )
            when (result) {
                is Resource.Success<*> -> {
                    Toast.makeText(this@ApprovalActivity, "Đã từ chối đơn của ${request.employeeFullName ?: "nhân viên"}!", Toast.LENGTH_SHORT).show()
                    allRequests.removeAll { it.employeeId == request.employeeId && it.fromDate == request.fromDate && it.absenceId == request.absenceId }
                    filterList()
                }
                is Resource.Error<*> -> {
                    Toast.makeText(this@ApprovalActivity, result.message ?: "Từ chối thất bại", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
}
