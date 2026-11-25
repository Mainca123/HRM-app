package com.example.baseproject.presentation.onboarding

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.baseproject.R
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.databinding.ActivityOnboardingBinding
import com.example.baseproject.presentation.auth.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Onboarding Activity adhering strictly to Coding Convention handbook & Figma design.
 * initViews() only calls modular sub-functions.
 */
@AndroidEntryPoint
class OnboardingActivity : BaseActivity<ActivityOnboardingBinding>() {

    private val onBoardingAdapter = OnBoardingAdapter()
    private val viewModel: OnBoardingViewModel by viewModels()

    override fun provideViewBinding(): ActivityOnboardingBinding = ActivityOnboardingBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        initViewPager()
        initListeners()
    }

    override fun initData() {
        super.initData()
        viewModel.getListOnBoarding()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.listOnBoarding.onEach {
            onBoardingAdapter.setData(ArrayList(it))
            updateButtonText(0)
            updateDots(0)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).launchIn(lifecycleScope)
    }

    private fun initViewPager() {
        viewBinding.vpOnBoarding.adapter = onBoardingAdapter

        viewBinding.vpOnBoarding.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.currentPosition = position
                updateButtonText(position)
                updateDots(position)
            }
        })
    }

    private fun initListeners() {
        viewBinding.btnNext.setOnClickListener {
            onNextClicked()
        }
    }

    private fun updateButtonText(position: Int) {
        if (position == onBoardingAdapter.dataSet.size - 1) {
            viewBinding.btnNext.text = getString(R.string.btn_batdau)
        } else {
            viewBinding.btnNext.text = getString(R.string.btn_tieptheo)
        }
    }

    private fun updateDots(position: Int) {
        val activeWidth = (resources.displayMetrics.density * 28).toInt()
        val inactiveWidth = (resources.displayMetrics.density * 6).toInt()

        viewBinding.dot1.layoutParams.width = if (position == 0) activeWidth else inactiveWidth
        viewBinding.dot1.background = AppCompatResources.getDrawable(this, if (position == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        viewBinding.dot1.requestLayout()

        viewBinding.dot2.layoutParams.width = if (position == 1) activeWidth else inactiveWidth
        viewBinding.dot2.background = AppCompatResources.getDrawable(this, if (position == 1) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        viewBinding.dot2.requestLayout()

        viewBinding.dot3.layoutParams.width = if (position == 2) activeWidth else inactiveWidth
        viewBinding.dot3.background = AppCompatResources.getDrawable(this, if (position == 2) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        viewBinding.dot3.requestLayout()
    }

    private fun onNextClicked() {
        if (viewModel.currentPosition < onBoardingAdapter.dataSet.size - 1) {
            viewBinding.vpOnBoarding.setCurrentItem(viewModel.currentPosition + 1, true)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    companion object {
        fun start(context: Context) {
            Intent(context, OnboardingActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }
}
