package com.example.baseproject.presentation.splash

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for Splash Screen.
 * Checks initial app state (language selected, onboarding seen, login status).
 */
class SplashViewModel : ViewModel() {

    private val _navigateToNext = MutableStateFlow<Destination?>(null)
    val navigateToNext: StateFlow<Destination?> = _navigateToNext.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        // TODO: Check DataStore/SharedPreferences to determine next screen
        // Default navigation to Language or Login
        _navigateToNext.value = Destination.LANGUAGE
    }

    enum class Destination {
        LANGUAGE, ONBOARDING, LOGIN, MAIN
    }
}
