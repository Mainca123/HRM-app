package com.example.baseproject

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import com.example.baseproject.utils.Constant
import com.example.baseproject.utils.LocaleHelper
import com.example.baseproject.utils.SpManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class App : Application() {
    @Inject
    lateinit var spManager: SpManager

    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        val savedLang = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constant.KEY_SP_CURRENT_LANGUAGE, "vi") ?: "vi"
        LocaleHelper.applyAppLocale(savedLang)
    }

    override fun attachBaseContext(base: Context?) {
        val savedLang = base?.let {
            PreferenceManager.getDefaultSharedPreferences(it)
                .getString(Constant.KEY_SP_CURRENT_LANGUAGE, "vi")
        } ?: "vi"
        val localizedContext = LocaleHelper.onAttach(base, savedLang)
        super.attachBaseContext(localizedContext)
    }
}
