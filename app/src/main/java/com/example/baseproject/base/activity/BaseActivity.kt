package com.example.baseproject.base.activity

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.baseproject.utils.LocaleHelper
import com.example.baseproject.utils.SpManager

/**
 * Base Activity with ViewBinding support and Edge-to-Edge / Fullscreen support (AF_ProjectBase standard).
 */
abstract class BaseActivity<V : ViewBinding> : AppCompatActivity() {
    lateinit var viewBinding: V

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }
        val spManager = SpManager(newBase)
        val lang = spManager.getLanguage() ?: "vi"
        val context = LocaleHelper.setLocale(newBase, lang) ?: newBase
        super.attachBaseContext(context)
    }

    open fun onBack() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = provideViewBinding()
        setContentView(viewBinding.root)

        setFullscreen()
        initViews()
        initData()
        initObserver()
        handleOnBackPressed()
    }

    open fun handleOnBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBack()
            }
        })
    }

    fun setFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
    }

    fun applySystemBarInsets(rootView: View, applyTop: Boolean = true, applyBottom: Boolean = true) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val topInset = if (applyTop) maxOf(statusBar.top, displayCutout.top) else 0
            val bottomInset = if (applyBottom) navBar.bottom else 0
            v.setPadding(v.paddingLeft, topInset, v.paddingRight, bottomInset)
            insets
        }
    }

    abstract fun provideViewBinding(): V

    open fun initViews() {}
    open fun initData() {}
    open fun initObserver() {}

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}
