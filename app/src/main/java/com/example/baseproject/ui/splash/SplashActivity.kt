package com.example.baseproject.ui.splash

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivitySplashBinding
import com.example.baseproject.ui.language.LanguageActivity
import com.example.baseproject.ui.login.LoginActivity
import com.example.baseproject.utils.SpManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash Activity managing first-time vs subsequent app flow.
 * First time: Splash -> Language -> Onboarding -> Login -> Main
 * Subsequent: Splash -> Login -> Main
 */
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    @Inject
    lateinit var spManager: SpManager

    override fun provideViewBinding(): ActivitySplashBinding =
        ActivitySplashBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        lifecycleScope.launch {
            delay(1500)
            if (spManager.isLanguageChosen()) {
                // Subsequent launches: skip Language & Onboarding -> Login
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            } else {
                // First launch: Language -> Onboarding -> Login
                startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
            }
            finish()
        }
    }
}
