package com.example.aichatassisstant.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.aichatassisstant.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getApiKey(): String {
        val buildConfigKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildConfigKey.isNotEmpty() && buildConfigKey !in PLACEHOLDER_KEYS) {
            return buildConfigKey
        }
        return preferences.getString(KEY_GEMINI_API_KEY, null)?.trim().orEmpty()
    }

    fun saveApiKey(key: String) {
        preferences.edit {
            putString(KEY_GEMINI_API_KEY, key.trim())
        }
    }

    fun isConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key !in PLACEHOLDER_KEYS
    }

    companion object {
        private const val PREFS_NAME = "chat_prefs"
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"

        private val PLACEHOLDER_KEYS = setOf(
            "your_actual_api_key_here",
            "YOUR_API_KEY_HERE",
            "AIzaSy_paste_your_key_here"
        )
    }
}
