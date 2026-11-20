package com.example.baseproject.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.base.fragment.BaseFragment
import com.example.baseproject.data.model.auth.UserProfile
import com.example.baseproject.databinding.FragmentProfileBinding
import com.example.baseproject.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun initViews() {
        super.initViews()
        initListeners()
    }

    private fun initListeners() {
        viewBinding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }

        viewBinding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Xác nhận") { _, _ ->
                val current = etCurrent.text.toString().trim()
                val newPass = etNew.text.toString().trim()

                if (current.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass.length < 6) {
                    Toast.makeText(requireContext(), "Mật khẩu mới phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.changePassword(current, newPass) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
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

    private fun renderUi(state: ProfileUiState) {
        state.userProfile?.let { user ->
            renderUserProfile(user)
        }

        if (state.isLoggedOut) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun renderUserProfile(user: UserProfile) {
        val fullName = user.fullName.ifBlank { user.username }
        viewBinding.tvFullName.text = fullName

        val parts = fullName.trim().split(" ")
        val initials = if (parts.size >= 2) {
            "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        } else {
            fullName.take(2).uppercase()
        }
        viewBinding.tvAvatarInitials.text = initials

        val code = user.employeeCode ?: "NV${user.employeeId ?: 1}"
        val role = user.role ?: "EMPLOYEE"
        viewBinding.tvRoleAndCode.text = "$code · $role"

        viewBinding.tvUsername.text = user.username
        viewBinding.tvEmployeeCode.text = code
        viewBinding.tvDepartment.text = "Phòng Kỹ thuật"
        viewBinding.tvRole.text = role
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }
}
