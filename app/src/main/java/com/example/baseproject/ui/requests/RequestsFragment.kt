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
import com.example.baseproject.utils.DialogActionItem
import com.example.baseproject.utils.DialogTone
import com.example.baseproject.utils.ModernDialogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RequestsFragment : BaseFragment<FragmentRequestsBinding>() {

    private val viewModel: RequestsViewModel by viewModels()

    private val leaveAdapter = LeaveRequestAdapter(onItemClick = { handleLeaveClick(it) })
    private val overtimeAdapter = OvertimeAdapter(onItemClick = { handleOvertimeClick(it) })
    private val explanationAdapter = ExplanationRequestAdapter(onItemClick = { handleExplanationClick(it) })
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
                ModernDialogHelper.showConfirm(
                    context = requireContext(),
                    title = "Xác nhận phê duyệt",
                    message = "Bạn có chắc chắn muốn phê duyệt đơn của ${request.employeeFullName ?: "nhân viên"} (${request.fromDate.take(10)})?",
                    positiveText = "Phê duyệt",
                    negativeText = getString(R.string.cancel),
                    tone = DialogTone.PRIMARY,
                    iconRes = R.drawable.ic_approve,
                    onPositive = { viewModel.approveRequest(request) }
                )
            },
            onRejectClick = { request ->
                ModernDialogHelper.showConfirm(
                    context = requireContext(),
                    title = "Xác nhận từ chối",
                    message = "Bạn có chắc chắn muốn từ chối đơn của ${request.employeeFullName ?: "nhân viên"} (${request.fromDate.take(10)})?",
                    positiveText = "Từ chối",
                    negativeText = getString(R.string.cancel),
                    tone = DialogTone.DANGER,
                    iconRes = R.drawable.ic_warning,
                    onPositive = { viewModel.rejectRequest(request) }
                )
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
        viewBinding.tabApproval.setOnClickListener {
            val roleType = com.example.baseproject.utils.RoleHelper.parseRole(viewModel.uiState.value.currentUser?.role)
            if (com.example.baseproject.utils.RoleHelper.isManagementRole(roleType)) {
                updateTabSelection(3)
            } else {
                Toast.makeText(requireContext(), "Bạn không có quyền duyệt đơn.", Toast.LENGTH_SHORT).show()
            }
        }

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
                viewBinding.rvRequests.adapter = explanationAdapter
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

                    // Phân quyền hiển thị tab Duyệt đơn
                    val roleType = com.example.baseproject.utils.RoleHelper.parseRole(state.currentUser?.role)
                    viewBinding.tabApproval.visibility = if (com.example.baseproject.utils.RoleHelper.isManagementRole(roleType)) View.VISIBLE else View.GONE

                    state.leaveBalance?.let { b ->
                        viewBinding.tvRemainingLeaveDays.text = "${b.remainingLeaveDays} ngày"
                        viewBinding.tvStandardLeave.text = "Tiêu chuẩn: ${b.standardLeaveDays}"
                        viewBinding.tvUsedLeave.text = "Đã dùng: ${b.usedLeaveDays}"
                    }

                    leaveAdapter.submitList(state.leaveRequests)
                    overtimeAdapter.submitList(state.overtimeRequests)
                    explanationAdapter.submitList(state.explanationRequests)
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
            2 -> state.explanationRequests.isEmpty()
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

    private fun handleLeaveClick(item: com.example.baseproject.data.model.leave.LeaveRequestDto) {
        if (!item.isPending) {
            Toast.makeText(requireContext(), "Đơn đã được xử lý, không thể chỉnh sửa.", Toast.LENGTH_SHORT).show()
            return
        }
        ModernDialogHelper.showActions(
            context = requireContext(),
            title = "Thao tác đơn nghỉ phép",
            subtitle = "${item.absenceTypeName.ifBlank { "Nghỉ phép" }} · ${item.fromDate.take(10)}",
            actions = listOf(
                DialogActionItem("Sửa đơn") {
                    val intent = Intent(requireContext(), CreateLeaveActivity::class.java).apply {
                        putExtra(CreateLeaveActivity.EXTRA_IS_EDIT, true)
                        putExtra(CreateLeaveActivity.EXTRA_EMPLOYEE_ID, item.employeeId)
                        putExtra(CreateLeaveActivity.EXTRA_ABSENCE_ID, item.absenceId)
                        putExtra(CreateLeaveActivity.EXTRA_FROM_DATE, item.fromDate)
                        putExtra(CreateLeaveActivity.EXTRA_TO_DATE, item.toDate ?: item.fromDate)
                        putExtra(CreateLeaveActivity.EXTRA_UNIT, item.daysCount)
                        putExtra(CreateLeaveActivity.EXTRA_NOTES, item.reason ?: "")
                    }
                    startActivity(intent)
                },
                DialogActionItem("Hủy đơn", isDestructive = true) {
                    ModernDialogHelper.showConfirm(
                        context = requireContext(),
                        title = "Xác nhận hủy đơn",
                        message = "Bạn có chắc chắn muốn hủy đơn nghỉ phép ngày ${item.fromDate.take(10)} không?",
                        positiveText = "Hủy đơn",
                        negativeText = getString(R.string.close),
                        tone = DialogTone.DANGER,
                        iconRes = R.drawable.ic_warning,
                        onPositive = { viewModel.cancelLeaveRequest(item) }
                    )
                }
            )
        )
    }

    private fun handleOvertimeClick(item: com.example.baseproject.data.model.overtime.OvertimeRequestDto) {
        if (item.isApproved) {
            Toast.makeText(requireContext(), "Đơn tăng ca đã được duyệt, không thể chỉnh sửa.", Toast.LENGTH_SHORT).show()
            return
        }
        ModernDialogHelper.showActions(
            context = requireContext(),
            title = "Thao tác đơn tăng ca",
            subtitle = "Ngày ${item.fromDate.take(10)} · OT${item.type} (${item.hours} giờ)",
            actions = listOf(
                DialogActionItem("Sửa số giờ") {
                    val intent = Intent(requireContext(), CreateOvertimeActivity::class.java).apply {
                        putExtra(CreateOvertimeActivity.EXTRA_IS_EDIT, true)
                        putExtra(CreateOvertimeActivity.EXTRA_EMPLOYEE_ID, item.employeeId)
                        putExtra(CreateOvertimeActivity.EXTRA_FROM_DATE, item.fromDate)
                        putExtra(CreateOvertimeActivity.EXTRA_TYPE, item.type)
                        putExtra(CreateOvertimeActivity.EXTRA_HOURS, item.hours)
                    }
                    startActivity(intent)
                },
                DialogActionItem("Xóa đơn", isDestructive = true) {
                    ModernDialogHelper.showConfirm(
                        context = requireContext(),
                        title = "Xác nhận xóa đơn",
                        message = "Bạn có chắc chắn muốn xóa đơn tăng ca ngày ${item.fromDate.take(10)} không?",
                        positiveText = "Xóa đơn",
                        negativeText = getString(R.string.close),
                        tone = DialogTone.DANGER,
                        iconRes = R.drawable.ic_warning,
                        onPositive = { viewModel.deleteOvertimeRequest(item) }
                    )
                }
            )
        )
    }

    private fun handleExplanationClick(item: com.example.baseproject.data.model.attendance.RegisterLateEarlyDto) {
        if (!item.modifiedUserName.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Đơn giải trình đã được duyệt, không thể xóa.", Toast.LENGTH_SHORT).show()
            return
        }
        ModernDialogHelper.showConfirm(
            context = requireContext(),
            title = "Xác nhận xóa đơn giải trình",
            message = "Bạn có muốn xóa đơn giải trình ngày ${item.fromDate.take(10)} để tạo lại không?",
            positiveText = "Xóa đơn",
            negativeText = getString(R.string.close),
            tone = DialogTone.DANGER,
            iconRes = R.drawable.ic_warning,
            onPositive = { viewModel.deleteExplanationRequest(item) }
        )
    }
}
