package com.example.baseproject.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * Locale Helper for changing app language dynamically (from AF_ProjectBase).
 */
object LocaleHelper {

    private const val DEFAULT_LANGUAGE = "en"

    fun onAttach(
        context: Context?,
        defaultLanguage: String?
    ): Context? {
        return setLocale(
            context = context,
            language = defaultLanguage
        )
    }

    fun setLocale(
        context: Context?,
        language: String?
    ): Context? {
        val safeLanguage = language
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_LANGUAGE

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(
                context = context,
                language = safeLanguage
            )
        } else {
            updateResourcesLegacy(
                context = context,
                language = safeLanguage
            )
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(
        context: Context?,
        language: String
    ): Context? {
        if (context == null) return null

        val locale = getLocale(language)

        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(
        context: Context?,
        language: String
    ): Context? {
        if (context == null) return null

        val locale = getLocale(language)

        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = resources.configuration

        configuration.locale = locale
        configuration.setLayoutDirection(locale)

        resources.updateConfiguration(
            configuration,
            resources.displayMetrics
        )

        return context
    }

    private fun getLocale(language: String): Locale {
        return if (language.contains("-")) {
            Locale.forLanguageTag(language)
        } else {
            Locale(language)
        }
    }
}
