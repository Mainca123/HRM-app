package com.example.baseproject.ui.login

import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.MainActivity
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible: Boolean = false

    override fun provideViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)
        initListeners()
    }

    private fun initListeners() {
        // Username input changes
        viewBinding.etUsername.doAfterTextChanged { text ->
            viewModel.onUsernameChanged(text?.toString() ?: "")
        }

        // Password input changes
        viewBinding.etPassword.doAfterTextChanged { text ->
            viewModel.onPasswordChanged(text?.toString() ?: "")
        }

        // Remember login checkbox
        viewBinding.cbRememberLogin.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onRememberLoginChanged(isChecked)
        }

        // Password visibility toggle
        viewBinding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Login button
        viewBinding.btnLogin.setOnClickListener {
            // Dismiss error message before logging in
            viewModel.clearError()
            viewModel.login()
        }

        // Forgot password
        viewBinding.tvForgotPassword.setOnClickListener {
            showToast("Vui lòng liên hệ bộ phận IT hoặc HR để được cấp lại mật khẩu.")
        }

        // Biometric login
        viewBinding.btnBiometric.setOnClickListener {
            showToast("Tính năng đăng nhập sinh trắc học đang được cấu hình.")
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        if (isPasswordVisible) {
            viewBinding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_eye)
            viewBinding.ivTogglePassword.alpha = 1.0f
        } else {
            viewBinding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            viewBinding.ivTogglePassword.setImageResource(R.drawable.ic_eye)
            viewBinding.ivTogglePassword.alpha = 0.5f
        }
        viewBinding.etPassword.setSelection(viewBinding.etPassword.text.length)
    }

    override fun initObserver() {
        super.initObserver()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderUiState(state)
                }
            }
        }
    }

    private fun renderUiState(state: LoginUiState) {
        // Autofill remembered username
        if (viewBinding.etUsername.text.isNullOrEmpty() && state.username.isNotEmpty()) {
            viewBinding.etUsername.setText(state.username)
            viewBinding.etUsername.setSelection(state.username.length)
        }

        // Set remember checkbox
        if (viewBinding.cbRememberLogin.isChecked != state.rememberLogin) {
            viewBinding.cbRememberLogin.isChecked = state.rememberLogin
        }

        // Loading state
        if (state.isLoading) {
            viewBinding.progressBarLogin.visibility = View.VISIBLE
            viewBinding.tvBtnLoginText.visibility = View.INVISIBLE
            viewBinding.btnLogin.isEnabled = false
        } else {
            viewBinding.progressBarLogin.visibility = View.GONE
            viewBinding.tvBtnLoginText.visibility = View.VISIBLE
            viewBinding.btnLogin.isEnabled = true
        }

        // Error handling
        if (!state.errorMessage.isNullOrBlank()) {
            viewBinding.tvErrorMessage.text = state.errorMessage
            viewBinding.tvErrorMessage.visibility = View.VISIBLE
        } else {
            viewBinding.tvErrorMessage.visibility = View.GONE
        }

        // Login success navigation
        state.loginSuccessUser?.let { user ->
            showToast("Chào mừng ${user.fullName.ifBlank { user.username }}!")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
