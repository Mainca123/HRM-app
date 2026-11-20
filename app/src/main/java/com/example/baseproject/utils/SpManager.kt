package com.example.baseproject.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.example.baseproject.data.model.Language

/**
 * SharedPreferences Manager helper (inspired by AF_ProjectBase).
 */
class SpManager(private val preferences: SharedPreferences) {
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

    fun getString(key: String, defaultValue: String): String? =
        preferences.getString(key, defaultValue)

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun saveLanguage(language: Language) {
        preferences.edit().putString(Constant.KEY_SP_CURRENT_LANGUAGE, language.languageCode).apply()
    }

    fun getLanguage(): String? {
        return preferences.getString(Constant.KEY_SP_CURRENT_LANGUAGE, "en")
    }

    fun setLanguageChosen() {
        preferences.edit().putBoolean(Constant.KEY_SP_LANGUAGE_CHOSEN, true).apply()
    }

    fun isLanguageChosen(): Boolean {
        return preferences.getBoolean(Constant.KEY_SP_LANGUAGE_CHOSEN, false)
    }
}
