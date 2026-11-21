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
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.profile_logout))
                .setMessage(getString(R.string.profile_logout_confirm))
                .setPositiveButton(getString(R.string.profile_logout)) { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

        viewBinding.layoutChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        viewBinding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Đã bật đăng nhập sinh trắc học" else "Đã tắt đăng nhập sinh trắc học"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        viewBinding.layoutLanguage.setOnClickListener {
            val spManager = SpManager(requireContext())
            val currentLang = spManager.getLanguage() ?: "vi"
            val languages = arrayOf("Tiếng Việt", "English")
            val langCodes = arrayOf("vi", "en")
            val currentIdx = if (currentLang == "en") 1 else 0

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_language))
                .setSingleChoiceItems(languages, currentIdx) { dialog, which ->
                    val selectedCode = langCodes[which]
                    if (selectedCode != currentLang) {
                        spManager.saveLanguageCode(selectedCode)
                        LocaleHelper.setLocale(requireContext(), selectedCode)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.language_changed, languages[which]),
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()

                        activity?.let { act ->
                            val intent = act.intent
                            intent.putExtra("selected_tab", R.id.nav_profile)
                            act.finish()
                            act.startActivity(intent)
                        }
                    } else {
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(getString(R.string.close), null)
                .show()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<EditText>(R.id.etNewPassword)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val current = etCurrent.text.toString().trim()
                val newPass = etNew.text.toString().trim()

                if (current.isEmpty() || newPass.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.profile_fill_all_fields), Toast.LENGTH_SHORT).show()
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
            .setNegativeButton(getString(R.string.cancel), null)
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
            "${parts.last().take(1)}${parts.first().take(1)}".uppercase()
        } else {
            fullName.take(2).uppercase()
        }
        viewBinding.tvAvatarInitials.text = initials

        val code = detail?.code ?: user.employeeCode ?: "NV00${user.employeeId ?: 125}"
        val roleType = com.example.baseproject.utils.RoleHelper.parseRole(user.role)
        val roleDisplayName = com.example.baseproject.utils.RoleHelper.getRoleDisplayName(roleType)
        val position = detail?.positionName?.ifBlank { null } ?: roleDisplayName
        viewBinding.tvRoleAndCode.text = "$code · $position"

        // Card 1
        val dept = detail?.departmentName?.ifBlank { null } ?: department ?: "Phòng Kỹ thuật"
        viewBinding.tvDepartment.text = dept
        viewBinding.tvPosition.text = position

        val rawDate = detail?.currentDepartmentFromDate
        val formattedDate = rawDate?.let { formatDate(it) } ?: "01/03/2023"
        viewBinding.tvJoinDate.text = formattedDate

        viewBinding.tvContractType.text = "Không thời hạn"

        val email = detail?.email?.ifBlank { null } ?: "${user.username.lowercase()}@congty.vn"
        viewBinding.tvEmail.text = email

        // Số điện thoại default
        viewBinding.tvPhone.text = "0912 345 678"
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
