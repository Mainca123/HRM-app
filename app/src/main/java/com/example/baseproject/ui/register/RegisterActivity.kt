package com.example.baseproject.ui.register

import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivityRegisterBinding

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    override fun provideViewBinding(): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(layoutInflater)

    override fun initViews() {
        viewBinding.btnRegister.setOnClickListener {
            showToast("Registration Successful")
            finish()
        }
    }
}
