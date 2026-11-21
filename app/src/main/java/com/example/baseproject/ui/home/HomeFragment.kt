package com.example.baseproject.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.baseproject.MainActivity
import com.example.baseproject.R
import com.example.baseproject.base.fragment.BaseFragment
import com.example.baseproject.data.model.dashboard.EmployeeDashboardDto
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.ui.forms.CreateExplanationActivity
import com.example.baseproject.ui.forms.CreateLeaveActivity
import com.example.baseproject.ui.forms.CreateOvertimeActivity
import com.example.baseproject.ui.management.ApprovalActivity
import com.example.baseproject.ui.management.DepartmentStatsActivity
import com.example.baseproject.ui.requests.LeaveRequestAdapter
import com.example.baseproject.ui.timesheet.TimesheetActivity
import com.example.baseproject.utils.RoleHelper
import com.example.baseproject.utils.RoleType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val viewModel: HomeViewModel by viewModels()
    private val leaveAdapter = LeaveRequestAdapter()

    private val clockHandler = Handler(Looper.getMainLooper())
    private val clockFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))

    private val clockRunnable = object : Runnable {
        override fun run() {
            viewBinding.tvRealtimeClock.text = clockFormat.format(Date())
            clockHandler.postDelayed(this, 1000)
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

    override fun initViews() {
        super.initViews()

        viewBinding.tvTodayDate.text = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

        // Setup RecyclerView
        viewBinding.rvRecentRequests.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = leaveAdapter
        }

        // Quyền truy cập ban đầu cho nút Menu Quản lý
        val initialUser = viewModel.uiState.value.currentUser
        val initialRole = RoleHelper.parseRole(initialUser?.role)
        viewBinding.btnRoleMenu.visibility = if (RoleHelper.isManagementRole(initialRole)) View.VISIBLE else View.GONE

        initListeners()
    }

    private fun initListeners() {
        viewBinding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDashboard()
        }

        viewBinding.btnCheckInOut.setOnClickListener {
            viewModel.handleCheckInOut()
        }

        // Quick action 1: Xin nghỉ
        viewBinding.actionLeave.setOnClickListener {
            startActivity(Intent(requireContext(), CreateLeaveActivity::class.java))
        }

        // Quick action 2: Đăng ký OT
        viewBinding.actionOvertime.setOnClickListener {
            startActivity(Intent(requireContext(), CreateOvertimeActivity::class.java))
        }

        // Quick action 3: Giải trình
        viewBinding.actionExplain.setOnClickListener {
            startActivity(Intent(requireContext(), CreateExplanationActivity::class.java))
        }

        // Quick action 4: Bảng công -> Open TimesheetActivity
        viewBinding.actionTimesheet.setOnClickListener {
            startActivity(Intent(requireContext(), TimesheetActivity::class.java))
        }

        viewBinding.tvViewAllRequests.setOnClickListener {
            (activity as? MainActivity)?.switchToTab(R.id.nav_requests)
        }

        viewBinding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Bạn không có thông báo mới nào.", Toast.LENGTH_SHORT).show()
        }

        viewBinding.btnRoleMenu.setOnClickListener {
            val user = viewModel.uiState.value.currentUser
            val roleType = RoleHelper.parseRole(user?.role)
            if (!RoleHelper.isManagementRole(roleType)) {
                Toast.makeText(requireContext(), "Bạn không có quyền quản lý.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val dept = viewModel.uiState.value.dashboard?.departmentName
            showRoleMenuDialog(user?.role, dept)
        }

        viewBinding.btnEmptyCreateLeave.setOnClickListener {
            startActivity(Intent(requireContext(), CreateLeaveActivity::class.java))
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

    private fun renderUi(state: HomeUiState) {
        viewBinding.swipeRefresh.isRefreshing = state.isLoading

        // User info from profile or dashboard
        val user = state.currentUser
        val fullName = state.dashboard?.employeeFullName?.ifBlank { null } ?: user?.fullName ?: "Nhân viên"
        viewBinding.tvGreetingName.text = "Xin chào, $fullName"

        val empCode = user?.employeeCode ?: "NV${user?.employeeId ?: 1}"
        val dept = state.dashboard?.departmentName ?: "Phòng ban"

        // Phân quyền vai trò người dùng (Role Management)
        val roleType = RoleHelper.parseRole(user?.role)
        val roleSubtitle = when (roleType) {
            RoleType.ADMIN -> "$empCode · Admin"
            RoleType.HR -> "$empCode · HR"
            RoleType.MANAGER -> "$empCode · Quản lý"
            RoleType.EMPLOYEE -> "$empCode · $dept"
        }
        viewBinding.tvEmployeeCodeDept.text = roleSubtitle

        // Nút menu quyền (4 ô vuông): CHỈ HIỂN THỊ cho Quản lý, HR, Admin; ẨN với nhân viên thông thường
        viewBinding.btnRoleMenu.visibility = if (RoleHelper.isManagementRole(roleType)) View.VISIBLE else View.GONE

        // Initials avatar
        val parts = fullName.trim().split(" ")
        val initials = if (parts.size >= 2) {
            "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        } else {
            fullName.take(2).uppercase()
        }
        viewBinding.tvAvatarInitials.text = initials

        // Render dashboard metrics
        state.dashboard?.let { d ->
            renderDashboardData(d)
        }

        // CheckInOut Loading & State
        if (state.isCheckingInOut) {
            viewBinding.pbCheckInOut.visibility = View.VISIBLE
            viewBinding.tvCheckInOutText.visibility = View.INVISIBLE
            viewBinding.btnCheckInOut.isEnabled = false
        } else {
            viewBinding.pbCheckInOut.visibility = View.GONE
            viewBinding.tvCheckInOutText.visibility = View.VISIBLE
            viewBinding.btnCheckInOut.isEnabled = true
        }

        // Feedback messages
        state.checkInOutMessage?.let { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }

        state.errorMessage?.let { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    private fun renderDashboardData(d: EmployeeDashboardDto) {
        viewBinding.tvShiftName.text = d.todayShiftName?.ifBlank { null } ?: "Ca HC: 08:00 - 17:00"

        // Check-in and out timestamps
        val timeIn = d.todayTimeIn?.takeLast(8) ?: "--:--:--"
        val timeOut = d.todayTimeOut?.takeLast(8) ?: "--:--:--"
        viewBinding.tvTimeIn.text = timeIn
        viewBinding.tvTimeOut.text = timeOut

        // Button state
        val hasCheckedIn = !d.todayTimeIn.isNullOrBlank()
        val hasCheckedOut = !d.todayTimeOut.isNullOrBlank()

        if (!hasCheckedIn) {
            viewBinding.tvCheckInOutText.text = "VÀO CA (CHECK-IN)"
            viewBinding.btnCheckInOut.backgroundTintList = ColorStateList.valueOf(0xFF1E40AF.toInt())
        } else if (!hasCheckedOut) {
            viewBinding.tvCheckInOutText.text = "TAN CA (CHECK-OUT)"
            viewBinding.btnCheckInOut.backgroundTintList = ColorStateList.valueOf(0xFFB45309.toInt())
        } else {
            viewBinding.tvCheckInOutText.text = "ĐÃ HOÀN THÀNH CA LÀM"
            viewBinding.btnCheckInOut.backgroundTintList = ColorStateList.valueOf(0xFF15803D.toInt())
        }

        // Metrics
        viewBinding.tvWorkDays.text = "${d.workDaysThisMonth} / 22"
        viewBinding.tvOvertimeHours.text = "${d.overtimeHoursThisMonth} giờ"
        viewBinding.tvLeaveBalance.text = "${d.annualLeaveBalance} ngày"
        viewBinding.tvLateMinutes.text = "${d.lateMinutesThisMonth} phút"

        // Recent leave requests
        val requests = d.recentLeaveRequests
        if (requests.isNullOrEmpty()) {
            viewBinding.rvRecentRequests.visibility = View.GONE
            viewBinding.layoutEmptyRequests.visibility = View.VISIBLE
        } else {
            viewBinding.rvRecentRequests.visibility = View.VISIBLE
            viewBinding.layoutEmptyRequests.visibility = View.GONE
            leaveAdapter.submitList(requests)
        }
    }

    private fun showRoleMenuDialog(role: String?, department: String?) {
        val roleType = RoleHelper.parseRole(role)
        if (!RoleHelper.isManagementRole(roleType)) {
            Toast.makeText(requireContext(), "Bạn không có quyền quản lý.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_role_menu, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvRoleMenuTitle)
        val tvSubtitle = dialogView.findViewById<TextView>(R.id.tvRoleMenuSubtitle)
        val btnClose = dialogView.findViewById<ImageView>(R.id.btnCloseRoleMenu)

        val itemApprove = dialogView.findViewById<View>(R.id.itemApprove)
        val itemDeptStats = dialogView.findViewById<View>(R.id.itemDeptStats)
        val itemHrOverview = dialogView.findViewById<View>(R.id.itemHrOverview)
        val itemHrReports = dialogView.findViewById<View>(R.id.itemHrReports)
        val itemSystemAdmin = dialogView.findViewById<View>(R.id.itemSystemAdmin)

        val dept = department ?: "Phòng Kỹ thuật"

        when (roleType) {
            RoleType.ADMIN -> {
                // Màn hình 25: Menu quyền — Admin
                tvTitle.text = "Chức năng quản trị"
                tvSubtitle.text = "Admin · Toàn công ty"
                itemApprove.visibility = View.VISIBLE
                itemDeptStats.visibility = View.GONE
                itemHrOverview.visibility = View.VISIBLE
                itemHrReports.visibility = View.VISIBLE
                itemSystemAdmin.visibility = View.VISIBLE
            }
            RoleType.HR -> {
                // Màn hình 24: Menu quyền — HR
                tvTitle.text = "Chức năng quản lý"
                tvSubtitle.text = "HR · Toàn công ty"
                itemApprove.visibility = View.VISIBLE
                itemDeptStats.visibility = View.GONE
                itemHrOverview.visibility = View.VISIBLE
                itemHrReports.visibility = View.VISIBLE
                itemSystemAdmin.visibility = View.GONE
            }
            RoleType.MANAGER -> {
                // Màn hình 23: Menu quyền — Quản lý
                tvTitle.text = "Chức năng quản lý"
                tvSubtitle.text = "Quản lý · $dept"
                itemApprove.visibility = View.VISIBLE
                itemDeptStats.visibility = View.VISIBLE
                itemHrOverview.visibility = View.GONE
                itemHrReports.visibility = View.GONE
                itemSystemAdmin.visibility = View.GONE
            }
            RoleType.EMPLOYEE -> {
                dialog.dismiss()
                return
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        // Action 1: Duyệt đơn -> Navigate to ApprovalActivity
        itemApprove.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(requireContext(), ApprovalActivity::class.java))
        }

        // Action 2: Thống kê phòng ban -> Navigate to DepartmentStatsActivity
        itemDeptStats.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), DepartmentStatsActivity::class.java).apply {
                putExtra("DEPT_NAME", dept)
            }
            startActivity(intent)
        }

        // Action 3: Tổng quan nhân sự
        itemHrOverview.setOnClickListener {
            dialog.dismiss()
            showFeatureDialog(
                "Tổng quan nhân sự",
                "Chức năng dành cho Quản lý & HR:\n• Theo dõi quân số đi làm, vắng mặt toàn công ty\n• Xem cơ cấu nhân sự theo từng khối phòng ban\n• Kiểm tra tình hình nghỉ phép năm và tỷ lệ chuyên cần"
            )
        }

        // Action 4: Báo cáo / Thống kê
        itemHrReports.setOnClickListener {
            dialog.dismiss()
            (activity as? MainActivity)?.switchToTab(R.id.nav_history)
        }

        // Action 5: Quản trị hệ thống
        itemSystemAdmin.setOnClickListener {
            dialog.dismiss()
            showFeatureDialog(
                "Quản trị hệ thống",
                "Phân hệ dành cho Quản trị viên (Admin):\n• Quản lý danh mục nhân viên & phân quyền tài khoản\n• Cài đặt ca làm việc, địa điểm & khoảng cách chấm công\n• Cấu hình ngày lễ & chính sách nghỉ phép\n• Xuất báo cáo chấm công & bảng lương tháng"
            )
        }

        dialog.show()
    }

    private fun showFeatureDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Đã hiểu", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        clockHandler.post(clockRunnable)
    }

    override fun onPause() {
        super.onPause()
        clockHandler.removeCallbacks(clockRunnable)
    }
}
