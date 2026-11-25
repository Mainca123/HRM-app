package com.example.baseproject.ui.language

import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivityLanguageBinding
import com.example.baseproject.ui.onboarding.OnboardingActivity
import com.example.baseproject.data.model.Language
import com.example.baseproject.utils.SpManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * Language Selection Activity adhering strictly to Coding Convention handbook.
 * initViews() only calls modular sub-functions.
 */
@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()
    private val languageAdapter = LanguageAdapter()
    private var selectLanguageModel: Language? = null

    @Inject
    lateinit var spManager: SpManager

    override fun provideViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        initAdapter()
        initListeners()
        initToolbar()
        updateContinueButtonState(false)
    }

    override fun initData() {
        super.initData()
        viewModel.loadListLanguage()
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.listLanguage.onEach {
            languageAdapter.setData(ArrayList(it))
        }.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).launchIn(lifecycleScope)
    }

    private fun initAdapter() {
        viewBinding.rclLanguage.adapter = languageAdapter
    }

    private fun initListeners() {
        languageAdapter.onClick = { language ->
            onLanguageItemClicked(language)
        }

        viewBinding.btnContinue.setOnClickListener {
            onContinueClicked()
        }
    }

    private fun initToolbar() {
        viewBinding.toolBar.tvTitle.text = getString(R.string.language)
    }

    private fun onLanguageItemClicked(language: Language) {
        languageAdapter.selectLanguage(language.languageCode)
        selectLanguageModel = languageAdapter.selectedLanguage()
        updateContinueButtonState(selectLanguageModel != null)
    }

    private fun onContinueClicked() {
        selectLanguageModel?.let { language ->
            spManager.saveLanguage(language)
            spManager.setLanguageChosen()
            com.example.baseproject.utils.LocaleHelper.applyAppLocale(language.languageCode)
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun updateContinueButtonState(isEnabled: Boolean) {
        viewBinding.btnContinue.isEnabled = isEnabled
        viewBinding.btnContinue.alpha = if (isEnabled) 1.0f else 0.5f
    }
}
