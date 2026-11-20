package com.example.baseproject.ui.forms

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.data.repositories.AttendanceRepository
import com.example.baseproject.databinding.ActivityCreateExplanationBinding
import com.example.baseproject.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CreateExplanationActivity : BaseActivity<ActivityCreateExplanationBinding>() {

    @Inject
    lateinit var attendanceRepository: AttendanceRepository

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun provideViewBinding(): ActivityCreateExplanationBinding =
        ActivityCreateExplanationBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()

        viewBinding.etWorkingDay.setText(dateFormat.format(calendar.time))
        initListeners()
    }

    private fun initListeners() {
        viewBinding.btnBack.setOnClickListener { finish() }

        viewBinding.etWorkingDay.setOnClickListener {
            val y = calendar.get(Calendar.YEAR)
            val m = calendar.get(Calendar.MONTH)
            val d = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                calendar.set(year, month, day)
                viewBinding.etWorkingDay.setText(dateFormat.format(calendar.time))
            }, y, m, d).show()
        }

        viewBinding.btnSubmit.setOnClickListener {
            submitExplanation()
        }
    }

    private fun submitExplanation() {
        val workingDay = viewBinding.etWorkingDay.text.toString().trim()
        val reason = viewBinding.etReason.text.toString().trim()

        if (reason.isEmpty()) {
            showToast("Vui lòng nhập lý do giải trình chi tiết")
            return
        }

        val type = when (viewBinding.rgExplanationType.checkedRadioButtonId) {
            R.id.rbEarly -> "EARLY"
            R.id.rbForgot -> "FORGOT"
            else -> "LATE"
        }

        viewBinding.btnSubmit.isEnabled = false
        viewBinding.btnSubmit.text = "ĐANG GỬI..."

        lifecycleScope.launch {
            when (val res = attendanceRepository.explainLateEarly(workingDay, reason, type)) {
                is Resource.Success -> {
                    Toast.makeText(this@CreateExplanationActivity, "Gửi đơn giải trình thành công!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is Resource.Error -> {
                    viewBinding.btnSubmit.isEnabled = true
                    viewBinding.btnSubmit.text = "GỬI GIẢI TRÌNH"
                    showToast(res.message ?: "Gửi giải trình thất bại")
                }
                is Resource.Loading -> {}
            }
        }
    }
}
