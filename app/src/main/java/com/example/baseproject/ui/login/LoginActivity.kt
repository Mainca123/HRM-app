package com.example.baseproject.ui.login

import android.content.Intent
import com.example.baseproject.MainActivity
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivityLoginBinding
import com.example.baseproject.ui.register.RegisterActivity

/**
 * Login Activity adhering strictly to coding convention handbook (separated initListeners, initObserver).
 */
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun provideViewBinding(): ActivityLoginBinding =
        ActivityLoginBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        initListeners()
    }

    private fun initListeners() {
        viewBinding.btnLogin.setOnClickListener {
            onLoginClicked()
        }

        viewBinding.tvRegisterLink.setOnClickListener {
            onRegisterLinkClicked()
        }
    }

    private fun onLoginClicked() {
        // TODO: Call API (Java Spring / ASP.NET) for login authentication
        showToast("Login Successful")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun onRegisterLinkClicked() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    override fun initObserver() {
        super.initObserver()
        // TODO: Observe login view model states here
    }
}
