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
import com.example.baseproject.data.model.employee.EmployeeDetailDto
import com.example.baseproject.databinding.FragmentProfileBinding
import com.example.baseproject.ui.login.LoginActivity
import com.example.baseproject.utils.LocaleHelper
import com.example.baseproject.utils.SpManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    override fun initViews() {
        super.initViews()
        val currentLang = SpManager(requireContext()).getLanguage() ?: "vi"
        viewBinding.tvLanguage.text = if (currentLang == "en") "English" else "Tiếng Việt"
        initListeners()
    }

    private fun initListeners() {
        viewBinding.layoutLogout.setOnClickListener {
            com.example.baseproject.utils.ModernDialogHelper.showConfirm(
                context = requireContext(),
                title = getString(R.string.profile_logout),
                message = getString(R.string.profile_logout_confirm),
                positiveText = getString(R.string.profile_logout),
                negativeText = getString(R.string.cancel),
                tone = com.example.baseproject.utils.DialogTone.DANGER,
                iconRes = R.drawable.ic_logout,
                onPositive = { viewModel.logout() }
            )
        }

        viewBinding.layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        viewBinding.layoutLanguage.setOnClickListener {
            val spManager = SpManager(requireContext())
            val currentLang = spManager.getLanguage() ?: "vi"
            val languages = arrayOf("Tiếng Việt", "English")
            val langCodes = arrayOf("vi", "en")
            val currentIdx = if (currentLang == "en") 1 else 0

            com.example.baseproject.utils.ModernDialogHelper.showActions(
                context = requireContext(),
                title = getString(R.string.select_language),
                actions = langCodes.mapIndexed { index, code ->
                    com.example.baseproject.utils.DialogActionItem(
                        label = languages[index],
                        isSelected = (index == currentIdx)
                    ) {
                        if (code != currentLang) {
                            spManager.saveLanguageCode(code)
                            LocaleHelper.setLocale(requireContext(), code)
                            LocaleHelper.applyAppLocale(code)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.language_changed, languages[index]),
                                Toast.LENGTH_SHORT
                            ).show()

                            activity?.let { act ->
                                val restartIntent = Intent(act, com.example.baseproject.MainActivity::class.java).apply {
                                    putExtra("selected_tab", R.id.nav_profile)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                act.startActivity(restartIntent)
                                act.finish()
                            }
                        }
                    }
                }
            )
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etConfirmPassword)
        val ivToggleCurrent = dialogView.findViewById<android.widget.ImageView>(R.id.ivToggleCurrentPass)
        val ivToggleNew = dialogView.findViewById<android.widget.ImageView>(R.id.ivToggleNewPass)
        val ivToggleConfirm = dialogView.findViewById<android.widget.ImageView>(R.id.ivToggleConfirmPass)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelPass)
        val btnConfirm = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnConfirmPass)

        fun bindPasswordToggle(editText: EditText, toggleView: android.widget.ImageView) {
            var visible = false
            toggleView.setOnClickListener {
                visible = !visible
                val sel = editText.selectionEnd
                editText.inputType = if (visible) {
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                if (sel >= 0) editText.setSelection(sel)
                toggleView.imageTintList = android.content.res.ColorStateList.valueOf(
                    if (visible) 0xFF1E40AF.toInt() else 0xFF94A3B8.toInt()
                )
            }
        }

        bindPasswordToggle(etCurrent, ivToggleCurrent)
        bindPasswordToggle(etNew, ivToggleNew)
        bindPasswordToggle(etConfirm, ivToggleConfirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            val current = etCurrent.text.toString().trim()
            val newPass = etNew.text.toString().trim()
            val confirmPass = etConfirm.text.toString().trim()

            if (current.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.profile_fill_all_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                Toast.makeText(requireContext(), "Mật khẩu mới phải từ 6 ký tự trở lên", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), getString(R.string.profile_password_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnConfirm.isEnabled = false
            viewModel.changePassword(current, newPass) { success, msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                if (success) {
                    dialog.dismiss()
                } else {
                    btnConfirm.isEnabled = true
                }
            }
        }

        dialog.show()
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
            renderProfile(user, state.employeeDetail, state.departmentName)
        }

        if (state.isLoggedOut) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun renderProfile(user: UserProfile, detail: EmployeeDetailDto?, department: String?) {
        val fullName = detail?.fullName?.ifBlank { null }
            ?: user.fullName.ifBlank { user.username }
        viewBinding.tvFullName.text = fullName

        val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
        val initials = if (parts.size >= 2) {
            "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        } else {
            fullName.take(2).uppercase()
        }
        viewBinding.tvAvatarInitials.text = initials

        val code = detail?.code ?: user.employeeCode ?: "NV${user.employeeId ?: 0}"
        val roleType = com.example.baseproject.utils.RoleHelper.parseRole(user.role)
        val isVi = (SpManager(requireContext()).getLanguage() ?: "vi") == "vi"
        val roleBadge = when (roleType) {
            com.example.baseproject.utils.RoleType.ADMIN -> "Admin"
            com.example.baseproject.utils.RoleType.HR -> "HR"
            com.example.baseproject.utils.RoleType.MANAGER -> if (isVi) "Quản lý" else "Manager"
            com.example.baseproject.utils.RoleType.EMPLOYEE -> if (isVi) "Nhân viên" else "Employee"
        }
        val roleDisplayName = when (roleType) {
            com.example.baseproject.utils.RoleType.ADMIN -> if (isVi) "Quản trị viên (Admin)" else "Administrator"
            com.example.baseproject.utils.RoleType.HR -> if (isVi) "Nhân sự (HR)" else "Human Resources"
            com.example.baseproject.utils.RoleType.MANAGER -> if (isVi) "Quản lý phòng ban" else "Department Manager"
            com.example.baseproject.utils.RoleType.EMPLOYEE -> if (isVi) "Nhân viên" else "Employee"
        }
        val rawPosition = detail?.positionName?.ifBlank { null }
        val position = localizePosition(rawPosition) ?: roleDisplayName
        viewBinding.tvRoleAndCode.text = "$code · $roleBadge"

        // Card 1: 100% real backend fields
        val notUpdated = getString(R.string.profile_not_updated)
        val dept = detail?.departmentName?.ifBlank { null } ?: department ?: notUpdated
        viewBinding.tvDepartment.text = dept
        viewBinding.tvPosition.text = position
        viewBinding.tvSystemRole.text = roleDisplayName

        val rawDate = detail?.currentDepartmentFromDate
        val formattedDate = rawDate?.let { formatDate(it) } ?: notUpdated
        viewBinding.tvJoinDate.text = formattedDate

        val email = detail?.email?.ifBlank { null } ?: notUpdated
        viewBinding.tvEmail.text = email
    }

    private fun localizePosition(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val isVi = (SpManager(requireContext()).getLanguage() ?: "vi") == "vi"
        if (!isVi) return raw
        return when (raw.trim().lowercase()) {
            "staff" -> "Nhân viên"
            "senior staff" -> "Nhân viên chính"
            "worker" -> "Công nhân"
            "leader" -> "Tổ trưởng"
            "a.leader" -> "Tổ phó"
            "supervisor" -> "Giám sát"
            "manager" -> "Quản lý"
            "project manager" -> "Quản lý dự án"
            "operations manager" -> "Quản lý vận hành"
            "engineer" -> "Kỹ sư"
            "senior engineer" -> "Kỹ sư chính"
            else -> raw
        }
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormats = arrayOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            )
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            for (format in inputFormats) {
                try {
                    val date = format.parse(dateStr)
                    if (date != null) return outputFormat.format(date)
                } catch (_: Exception) {}
            }
            dateStr.take(10)
        } catch (_: Exception) {
            dateStr
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadUserProfile()
    }
}
