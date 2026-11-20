package com.example.baseproject

import com.example.baseproject.base.activity.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding

/**
 * Main Dashboard Activity after successful login.
 * Conforms to coding convention handbook (separated initViews, initListeners, initObserver).
 */
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun provideViewBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        initListeners()
    }

    private fun initListeners() {
        // TODO: Initialize click listeners and view interactions here
    }

    override fun initData() {
        super.initData()
        // TODO: Load main dashboard data here
    }

    override fun initObserver() {
        super.initObserver()
        // TODO: Observe ViewModel live data or flows here
    }
}
