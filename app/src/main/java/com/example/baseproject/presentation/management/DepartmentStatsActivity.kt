package com.example.baseproject.presentation.management

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.data.repository.AuthRepository
import com.example.baseproject.data.repository.DashboardRepository
import com.example.baseproject.data.repository.OvertimeRepository
import com.example.baseproject.databinding.ActivityDepartmentStatsBinding
import com.example.baseproject.core.common.Resource
import com.example.baseproject.core.helper.RoleHelper
import com.example.baseproject.core.helper.RoleType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DepartmentStatsActivity : BaseActivity<ActivityDepartmentStatsBinding>() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var overtimeRepository: OvertimeRepository

    private val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))
    private var departmentName: String = "Phòng Kỹ thuật"

    override fun provideViewBinding(): ActivityDepartmentStatsBinding =
        ActivityDepartmentStatsBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        departmentName = intent.getStringExtra("DEPT_NAME") ?: "Phòng Kỹ thuật"
        viewBinding.tvDeptName.text = departmentName
        viewBinding.tvDataScope.text = "Phạm vi dữ liệu: $departmentName"
        viewBinding.tvDate.text = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

        initListeners()
        loadData()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.btnViewApproval.setOnClickListener {
            val intent = Intent(this, ApprovalActivity::class.java).apply {
                putExtra("DEPT_NAME", departmentName)
            }
            startActivity(intent)
        }
    }

    private fun loadData() {
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
                }
            }

            val otRes = overtimeRepository.getOvertimeRequests()
            val pendingOtCount = if (otRes is Resource.Success) {
                otRes.data?.count {
                    !it.isApproved &&
                        it.employeeId != currentEmpId &&
                        (myDeptName.isNullOrBlank() || it.departmentName.equals(myDeptName, ignoreCase = true))
                } ?: 0
            } else 0

            when (val result = dashboardRepository.getManagerDashboard(myDeptId)) {
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        val scope = data.departmentName ?: myDeptName ?: if (roleType == RoleType.MANAGER) departmentName else "Toàn công ty"
                        viewBinding.tvDeptName.text = scope
                        viewBinding.tvDataScope.text =
                            "Phạm vi: $scope · Có mặt: ${data.presentToday}/${data.totalDepartmentEmployees}"
                        viewBinding.tvAttendanceRate.text = "${data.attendanceRate}%"

                        val leaveCount = if (data.pendingLeaves.isNotEmpty()) {
                            data.pendingLeaves.count { it.isPending && it.employeeId != currentEmpId }
                        } else {
                            data.pendingLeaveRequestsCount
                        }
                        val explainCount = if (data.pendingLateEarlies.isNotEmpty()) {
                            data.pendingLateEarlies.count { it.modifiedUserName.isNullOrBlank() && it.employeeId != currentEmpId }
                        } else {
                            data.pendingLateEarlyRequestsCount
                        }

                        val totalPending = leaveCount + pendingOtCount + explainCount
                        viewBinding.tvPendingCountHeader.text = "Đơn cần xử lý · $totalPending"
                        viewBinding.tvPendingLeaveCount.text = "$leaveCount đơn"
                        viewBinding.tvPendingOtCount.text = "$pendingOtCount đơn"
                        viewBinding.tvPendingExplainCount.text = "$explainCount đơn"
                    }
                }
                is Resource.Error -> {
                    if (roleType == RoleType.ADMIN || roleType == RoleType.HR) {
                        when (val hrRes = dashboardRepository.getHrDashboard()) {
                            is Resource.Success -> {
                                val hr = hrRes.data
                                if (hr != null) {
                                    val rate = if (hr.totalCompanyEmployees > 0) {
                                        ((hr.companyPresentToday * 100.0) / hr.totalCompanyEmployees).toInt()
                                    } else 0
                                    viewBinding.tvDeptName.text = "Toàn công ty (${hr.totalDepartments} phòng ban)"
                                    viewBinding.tvDataScope.text =
                                        "Có mặt: ${hr.companyPresentToday}/${hr.totalCompanyEmployees} · Vắng: ${hr.companyAbsentToday}"
                                    viewBinding.tvAttendanceRate.text = "$rate%"
                                    viewBinding.tvPendingCountHeader.text = "Đơn cần xử lý · ${hr.pendingApprovalsCount + pendingOtCount}"
                                    viewBinding.tvPendingLeaveCount.text = "${hr.pendingApprovalsCount} đơn"
                                    viewBinding.tvPendingOtCount.text = "$pendingOtCount đơn"
                                    viewBinding.tvPendingExplainCount.text = "0 đơn"
                                    return@launch
                                }
                            }
                            else -> {}
                        }
                    }
                    Toast.makeText(this@DepartmentStatsActivity, result.message ?: "Lỗi tải thống kê", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
