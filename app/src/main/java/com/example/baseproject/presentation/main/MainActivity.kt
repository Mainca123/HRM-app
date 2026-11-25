package com.example.baseproject.presentation.main

import com.example.baseproject.R
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.baseproject.core.base.BaseActivity
import com.example.baseproject.databinding.ActivityMainBinding
import com.example.baseproject.presentation.attendance.history.HistoryFragment
import com.example.baseproject.presentation.home.HomeFragment
import com.example.baseproject.presentation.profile.ProfileFragment
import com.example.baseproject.presentation.requests.RequestsFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Screen hosting the primary HRM navigation tabs.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val homeFragment by lazy { HomeFragment() }
    private val historyFragment by lazy { HistoryFragment() }
    private val requestsFragment by lazy { RequestsFragment() }
    private val profileFragment by lazy { ProfileFragment() }

    private var activeFragment: Fragment = homeFragment

    override fun provideViewBinding(): ActivityMainBinding =
        ActivityMainBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()

        // Handle Window Insets for edge-to-edge display (Safe Area for camera punch-hole and gesture bar)
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.mainLayout) { _, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())

            val topInset = maxOf(statusBar.top, displayCutout.top)
            viewBinding.navHostFragment.updatePadding(top = topInset)
            viewBinding.bottomNavigation.updatePadding(bottom = navBar.bottom)
            insets
        }

        // Setup fragment container with HomeFragment initially
        supportFragmentManager.beginTransaction()
            .add(R.id.navHostFragment, homeFragment, "HOME")
            .commit()

        initListeners()

        val targetTab = intent.getIntExtra("selected_tab", -1)
        if (targetTab != -1) {
            viewBinding.bottomNavigation.selectedItemId = targetTab
        }
    }

    private fun initListeners() {
        viewBinding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchFragment(homeFragment, "HOME")
                    true
                }
                R.id.nav_history -> {
                    switchFragment(historyFragment, "HISTORY")
                    true
                }
                R.id.nav_requests -> {
                    switchFragment(requestsFragment, "REQUESTS")
                    true
                }
                R.id.nav_profile -> {
                    switchFragment(profileFragment, "PROFILE")
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment, tag: String) {
        if (activeFragment == targetFragment) return

        val transaction = supportFragmentManager.beginTransaction()
        val existingFragment = supportFragmentManager.findFragmentByTag(tag)

        if (existingFragment == null) {
            transaction.add(R.id.navHostFragment, targetFragment, tag)
        }

        transaction.hide(activeFragment).show(targetFragment).commit()
        activeFragment = targetFragment
    }

    fun switchToTab(menuItemId: Int, subTabIndex: Int? = null) {
        viewBinding.bottomNavigation.selectedItemId = menuItemId
        if (subTabIndex != null && menuItemId == R.id.nav_requests) {
            requestsFragment.selectSubTab(subTabIndex)
        }
    }
}
