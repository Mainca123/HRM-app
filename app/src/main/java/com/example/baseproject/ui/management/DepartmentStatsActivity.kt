package com.example.baseproject.ui.management

import android.content.Intent
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.repositories.AuthRepository
import com.example.baseproject.databinding.ActivityDepartmentStatsBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DepartmentStatsActivity : BaseActivity<ActivityDepartmentStatsBinding>() {

    @Inject
    lateinit var authRepository: AuthRepository

    private val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi", "VN"))

    override fun provideViewBinding(): ActivityDepartmentStatsBinding =
        ActivityDepartmentStatsBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        applySystemBarInsets(viewBinding.root)

        val dept = intent.getStringExtra("DEPT_NAME") ?: "Phòng Kỹ thuật"
        viewBinding.tvDeptName.text = dept
        viewBinding.tvDataScope.text = "Phạm vi dữ liệu: $dept"
        viewBinding.tvDate.text = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.btnViewApproval.setOnClickListener {
            val intent = Intent(this, ApprovalActivity::class.java).apply {
                putExtra("DEPT_NAME", dept)
            }
            startActivity(intent)
        }
    }
}