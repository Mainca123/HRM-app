package com.example.baseproject.ui.home

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
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
import com.example.baseproject.data.model.dashboard.EmployeeDashboardDto
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.ui.forms.CreateExplanationActivity
import com.example.baseproject.ui.forms.CreateLeaveActivity
import com.example.baseproject.ui.forms.CreateOvertimeActivity
import com.example.baseproject.ui.requests.LeaveRequestAdapter
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

        // Quick action 4: Bảng công -> Switch to History/Timesheet tab
        viewBinding.actionTimesheet.setOnClickListener {
            (activity as? com.example.baseproject.MainActivity)?.switchToTab(R.id.nav_history)
        }

        viewBinding.tvViewAllRequests.setOnClickListener {
            (activity as? com.example.baseproject.MainActivity)?.switchToTab(R.id.nav_requests)
        }

        viewBinding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Bạn có 2 thông báo mới về ca làm việc và đơn nghỉ.", Toast.LENGTH_SHORT).show()
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
        val dept = state.dashboard?.departmentName ?: "Phòng Kỹ thuật"
        viewBinding.tvEmployeeCodeDept.text = "$empCode · $dept"

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
        leaveAdapter.submitList(d.recentLeaveRequests)
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
