package com.example.baseproject.presentation.management

import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.data.remote.dto.overtime.UpdateOvertimeRequestDto
import com.example.baseproject.data.repository.AttendanceRepository
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.data.repository.DashboardRepository
import com.example.baseproject.data.repository.LeaveRepository
import com.example.baseproject.data.repository.OvertimeRepository
import com.example.baseproject.databinding.ActivityApprovalBinding
import com.example.baseproject.core.helper.DialogTone
import com.example.baseproject.core.helper.ModernDialogHelper
import com.example.baseproject.core.common.Resource
import com.example.baseproject.core.helper.RoleHelper
import com.example.baseproject.core.helper.RoleType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApprovalActivity : BaseActivity<ActivityApprovalBinding>() {

    @Inject
    lateinit var leaveRepository: LeaveRepository

    @Inject
    lateinit var overtimeRepository: OvertimeRepository

    @Inject
    lateinit var attendanceRepository: AttendanceRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

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
            val user = authRepository.getUserProfile()
            val currentEmpId = user?.employeeId
            val roleType = RoleHelper.parseRole(user?.role)

            var myDeptId: String? = null
            var myDeptName: String? = null
            if (roleType == RoleType.MANAGER && currentEmpId != null) {
                val empRes = authRepository.getEmployeeDetail(currentEmpId)
                if (empRes is Resource.Success) {
                    myDeptId = empRes.data?.departmentId
                    myDeptName = empRes.data?.departmentName
                    if (!myDeptName.isNullOrBlank()) {
                        viewBinding.tvDeptHeader.text = myDeptName
                    }
                }
            } else if (roleType == RoleType.ADMIN || roleType == RoleType.HR) {
                viewBinding.tvDeptHeader.text = "Toàn công ty"
            }

            val combined = mutableListOf<LeaveRequestDto>()

            val mgrRes = dashboardRepository.getManagerDashboard(myDeptId)
            val mgrData = if (mgrRes is Resource.Success) mgrRes.data else null

            // 1. Leave requests (merge ManagerDashboard + LeaveRequests for Admin/HR/Manager)
            val leaveMap = linkedMapOf<String, LeaveRequestDto>()
            mgrData?.pendingLeaves
                ?.filter { it.isPending && it.employeeId != currentEmpId }
                ?.forEach { leaveMap["${it.employeeId}|${it.fromDate.take(10)}|${it.absenceId}"] = it }

            val leaveRes = leaveRepository.getLeaveRequests()
            if (leaveRes is Resource.Success) {
                leaveRes.data.orEmpty()
                    .filter { it.isPending && it.employeeId != currentEmpId }
                    .forEach { leaveMap["${it.employeeId}|${it.fromDate.take(10)}|${it.absenceId}"] = it }
            }
            combined.addAll(leaveMap.values)

            // 2. Overtime requests
            val otRes = overtimeRepository.getOvertimeRequests()
            if (otRes is Resource.Success) {
                combined.addAll(
                    otRes.data.orEmpty()
                        .filter { !it.isApproved && it.employeeId != currentEmpId }
                        .map { ot ->
                            LeaveRequestDto(
                                employeeId = ot.employeeId,
                                employeeCode = ot.employeeCode,
                                employeeFullName = ot.employeeFullName,
                                departmentName = ot.departmentName,
                                fromDate = ot.fromDate,
                                toDate = ot.toDate ?: ot.fromDate,
                                unit = ot.hours,
                                absenceId = "OT:${ot.type}",
                                absenceName = "Tăng ca OT${ot.type} (${ot.overtimeTypeName})",
                                notes = "${ot.hours} giờ",
                                status = 0
                            )
                        }
                )
            }

            // 3. Explanation requests (merge ManagerDashboard + ExplainLateEarly)
            val explainMap = linkedMapOf<String, com.example.baseproject.data.remote.dto.attendance.RegisterLateEarlyDto>()
            mgrData?.pendingLateEarlies
                ?.filter { it.modifiedUserName.isNullOrBlank() && it.employeeId != currentEmpId }
                ?.forEach { explainMap["${it.employeeId}|${it.fromDate.take(10)}|${it.type}"] = it }

            val explainRes = attendanceRepository.getLateEarlyRequests()
            if (explainRes is Resource.Success) {
                explainRes.data.orEmpty()
                    .filter { it.modifiedUserName.isNullOrBlank() && it.employeeId != currentEmpId }
                    .forEach { explainMap["${it.employeeId}|${it.fromDate.take(10)}|${it.type}"] = it }
            }
            explainMap.values.forEach { ex ->
                val typeLabel = ex.typeName ?: if (ex.type) "Về sớm" else "Đi muộn"
                combined.add(
                    LeaveRequestDto(
                        employeeId = ex.employeeId,
                        employeeCode = ex.employeeCode,
                        employeeFullName = ex.employeeFullName,
                        departmentName = ex.departmentName,
                        fromDate = ex.fromDate,
                        toDate = ex.toDate ?: ex.fromDate,
                        unit = (ex.value ?: 0).toDouble(),
                        absenceId = "EXPLAIN:${ex.type}",
                        absenceName = "Giải trình ($typeLabel)",
                        notes = ex.reason ?: "Không có lý do",
                        status = 0
                    )
                )
            }

            viewBinding.swipeRefresh.isRefreshing = false
            val scopedList = if (roleType == RoleType.MANAGER && !myDeptName.isNullOrBlank()) {
                combined.filter { it.departmentName.equals(myDeptName, ignoreCase = true) }
            } else {
                combined
            }
            allRequests.clear()
            allRequests.addAll(scopedList)
            filterList()
        }
    }

    private fun filterList() {
        val leaveList = allRequests.filter { !it.absenceId.startsWith("OT:") && !it.absenceId.startsWith("EXPLAIN:") }
        val otList = allRequests.filter { it.absenceId.startsWith("OT:") }
        val explainList = allRequests.filter { it.absenceId.startsWith("EXPLAIN:") }

        viewBinding.tabFilterLeave.text = "Nghỉ phép (${leaveList.size})"
        viewBinding.tabFilterOvertime.text = "Tăng ca (${otList.size})"
        viewBinding.tabFilterExplain.text = "Giải trình (${explainList.size})"

        val filtered = when (currentFilter) {
            0 -> leaveList
            1 -> otList
            else -> explainList
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
        ModernDialogHelper.showConfirm(
            context = this,
            title = "Xác nhận phê duyệt",
            message = "Bạn có chắc chắn muốn phê duyệt đơn của ${request.employeeFullName ?: "nhân viên"} (${request.fromDate.take(10)})?",
            positiveText = "Phê duyệt",
            negativeText = getString(R.string.cancel),
            tone = DialogTone.PRIMARY,
            iconRes = R.drawable.ic_approve,
            onPositive = {
                lifecycleScope.launch {
                    val errorMsg: String? = when {
                        request.absenceId.startsWith("OT:") -> {
                            val otType = request.absenceId.removePrefix("OT:").toByteOrNull() ?: 1
                            when (val r = overtimeRepository.updateOvertimeRequest(
                                UpdateOvertimeRequestDto(
                                    employeeId = request.employeeId,
                                    fromDate = request.fromDate.take(10),
                                    type = otType,
                                    value = request.unit ?: 1.0
                                )
                            )) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Phê duyệt OT thất bại"
                                else -> null
                            }
                        }
                        request.absenceId.startsWith("EXPLAIN:") -> {
                            val isEarly = request.absenceId.removePrefix("EXPLAIN:").toBooleanStrictOrNull() ?: false
                            when (val r = attendanceRepository.approveLateEarlyRequest(
                                employeeId = request.employeeId,
                                fromDate = request.fromDate.take(10),
                                type = isEarly
                            )) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Phê duyệt giải trình thất bại"
                                else -> null
                            }
                        }
                        else -> {
                            when (val r = leaveRepository.approveLeave(request.employeeId, request.fromDate, request.absenceId)) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Phê duyệt thất bại"
                                else -> null
                            }
                        }
                    }

                    if (errorMsg == null) {
                        Toast.makeText(this@ApprovalActivity, "Đã duyệt đơn của ${request.employeeFullName ?: "nhân viên"}!", Toast.LENGTH_SHORT).show()
                        allRequests.removeAll { it.employeeId == request.employeeId && it.fromDate == request.fromDate && it.absenceId == request.absenceId }
                        filterList()
                    } else {
                        Toast.makeText(this@ApprovalActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    private fun handleReject(request: LeaveRequestDto) {
        ModernDialogHelper.showConfirm(
            context = this,
            title = "Xác nhận từ chối",
            message = "Bạn có chắc chắn muốn từ chối đơn của ${request.employeeFullName ?: "nhân viên"} (${request.fromDate.take(10)})?",
            positiveText = "Từ chối",
            negativeText = getString(R.string.cancel),
            tone = DialogTone.DANGER,
            iconRes = R.drawable.ic_warning,
            onPositive = {
                lifecycleScope.launch {
                    val errorMsg: String? = when {
                        request.absenceId.startsWith("OT:") -> {
                            val otType = request.absenceId.removePrefix("OT:").toByteOrNull() ?: 1
                            when (val r = overtimeRepository.deleteOvertimeRequest(
                                employeeId = request.employeeId,
                                fromDate = request.fromDate.take(10),
                                type = otType
                            )) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Từ chối OT thất bại"
                                else -> null
                            }
                        }
                        request.absenceId.startsWith("EXPLAIN:") -> {
                            val isEarly = request.absenceId.removePrefix("EXPLAIN:").toBooleanStrictOrNull() ?: false
                            when (val r = attendanceRepository.deleteLateEarlyRequest(
                                employeeId = request.employeeId,
                                fromDate = request.fromDate.take(10),
                                type = isEarly
                            )) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Từ chối giải trình thất bại"
                                else -> null
                            }
                        }
                        else -> {
                            when (val r = leaveRepository.rejectLeave(
                                request.employeeId,
                                request.fromDate,
                                request.absenceId,
                                "Không phù hợp kế hoạch công tác"
                            )) {
                                is Resource.Success -> null
                                is Resource.Error -> r.message ?: "Từ chối thất bại"
                                else -> null
                            }
                        }
                    }

                    if (errorMsg == null) {
                        Toast.makeText(this@ApprovalActivity, "Đã từ chối đơn của ${request.employeeFullName ?: "nhân viên"}!", Toast.LENGTH_SHORT).show()
                        allRequests.removeAll { it.employeeId == request.employeeId && it.fromDate == request.fromDate && it.absenceId == request.absenceId }
                        filterList()
                    } else {
                        Toast.makeText(this@ApprovalActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}
