package com.example.baseproject.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.baseproject.data.model.Language
import com.example.baseproject.data.model.auth.UserProfile
import com.google.gson.Gson

/**
 * SharedPreferences Manager helper for HRM Application.
 */
class SpManager(private val preferences: SharedPreferences) {
    private val gson = Gson()

    companion object {
        private var instance: SpManager? = null

        fun getInstance(context: Context): SpManager {
            if (instance == null) {
                instance = SpManager(PreferenceManager.getDefaultSharedPreferences(context))
            }
            return instance!!
        }
    }

    fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        preferences.getBoolean(key, defaultValue)

    fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)

    fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long =
        preferences.getLong(key, defaultValue)

    fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    fun getString(key: String, defaultValue: String = ""): String =
        preferences.getString(key, defaultValue) ?: defaultValue

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    // Language
    fun saveLanguage(language: Language) {
        preferences.edit().putString(Constant.KEY_SP_CURRENT_LANGUAGE, language.languageCode).apply()
    }

    fun getLanguage(): String? {
        return preferences.getString(Constant.KEY_SP_CURRENT_LANGUAGE, "vi")
    }

    fun setLanguageChosen() {
        preferences.edit().putBoolean(Constant.KEY_SP_LANGUAGE_CHOSEN, true).apply()
    }

    fun isLanguageChosen(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_LANGUAGE_CHOSEN, false)
    }

    // Onboarding
    fun setOnboardingCompleted() {
        preferences.edit().putBoolean(Constant.KEY_SP_ONBOARDING_COMPLETED, true).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_ONBOARDING_COMPLETED, false)
    }

    // Auth & Session
    fun saveAccessToken(token: String) {
        preferences.edit().putString(Constant.KEY_SP_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String {
        return preferences.getString(Constant.KEY_SP_ACCESS_TOKEN, "") ?: ""
    }

    fun clearAccessToken() {
        preferences.edit().remove(Constant.KEY_SP_ACCESS_TOKEN).apply()
    }

    fun saveUserProfile(user: UserProfile) {
        val json = gson.toJson(user)
        preferences.edit().putString(Constant.KEY_SP_USER_PROFILE, json).apply()
    }

    fun getUserProfile(): UserProfile? {
        val json = preferences.getString(Constant.KEY_SP_USER_PROFILE, null) ?: return null
        return try {
            gson.fromJson(json, UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun clearUserProfile() {
        preferences.edit().remove(Constant.KEY_SP_USER_PROFILE).apply()
    }

    fun setRememberLogin(remember: Boolean) {
        preferences.edit().putBoolean(Constant.KEY_SP_REMEMBER_LOGIN, remember).apply()
    }

    fun isRememberLogin(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_REMEMBER_LOGIN, false)
    }

    fun setSavedUsername(username: String) {
        preferences.edit().putString(Constant.KEY_SP_SAVED_USERNAME, username).apply()
    }

    fun getSavedUsername(): String {
        return preferences.getString(Constant.KEY_SP_SAVED_USERNAME, "") ?: ""
    }

    fun setLoggedIn(isLoggedIn: Boolean) {
        preferences.edit().putBoolean(Constant.KEY_SP_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_IS_LOGGED_IN, false) && getAccessToken().isNotBlank()
    }

    fun logout() {
        val remember = isRememberLogin()
        val savedUsername = getSavedUsername()
        preferences.edit()
            .remove(Constant.KEY_SP_ACCESS_TOKEN)
            .remove(Constant.KEY_SP_USER_PROFILE)
            .putBoolean(Constant.KEY_SP_IS_LOGGED_IN, false)
            .apply()

        if (remember) {
            setSavedUsername(savedUsername)
            setRememberLogin(true)
        } else {
            preferences.edit().remove(Constant.KEY_SP_SAVED_USERNAME).apply()
        }
    }
}
