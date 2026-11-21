package com.example.baseproject.ui.management

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.data.repositories.DashboardRepository
import com.example.baseproject.databinding.ActivityDepartmentStatsBinding
import com.example.baseproject.utils.Resource
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
            when (val result = dashboardRepository.getManagerDashboard()) {
                is Resource.Success -> {
                    val data = result.data
                    if (data != null) {
                        viewBinding.tvDeptName.text = data.departmentName ?: departmentName
                        viewBinding.tvDataScope.text = "Phạm vi dữ liệu: ${data.departmentName ?: departmentName}"
                        viewBinding.tvAttendanceRate.text = "${data.attendanceRate}%"

                        val totalPending = data.pendingLeaveRequestsCount + data.pendingLateEarlyRequestsCount
                        viewBinding.tvPendingCountHeader.text = "Đơn cần xử lý · $totalPending"
                        viewBinding.tvPendingLeaveCount.text = "${data.pendingLeaveRequestsCount} đơn"
                        viewBinding.tvPendingOtCount.text = "0 đơn"
                        viewBinding.tvPendingExplainCount.text = "${data.pendingLateEarlyRequestsCount} đơn"
                    }
                }
                is Resource.Error -> {
                    Toast.makeText(this@DepartmentStatsActivity, result.message ?: "Lỗi tải thống kê", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
