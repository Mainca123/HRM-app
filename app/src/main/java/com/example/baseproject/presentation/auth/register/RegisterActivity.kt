package com.example.baseproject.presentation.auth.register

import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.databinding.ActivityRegisterBinding

class RegisterActivity : BaseActivity<ActivityRegisterBinding>() {

    override fun provideViewBinding(): ActivityRegisterBinding =
        ActivityRegisterBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        viewBinding.btnRegister.setOnClickListener {
            showToast("Tài khoản nhân viên do bộ phận HR/Admin cấp. Vui lòng liên hệ quản trị viên.")
            finish()
        }
    }
}
