package com.example.aichatassisstant.data.remote

import com.example.aichatassisstant.data.local.ApiKeyManager
import com.example.aichatassisstant.data.remote.dto.GeminiErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiErrorMapper @Inject constructor(
    private val apiKeyManager: ApiKeyManager
) {

    private val gson = Gson()

    fun mapException(exception: Exception): String {
        if (!apiKeyManager.isConfigured()) {
            return "API key not set. Tap the key icon in the toolbar to add your Gemini API key."
        }

        if (exception is HttpException) {
            return mapHttpException(exception)
        }

        return exception.message ?: "Something went wrong. Please try again."
    }

    private fun mapHttpException(exception: HttpException): String {
        val errorBody = exception.response()?.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            runCatching {
                gson.fromJson(errorBody, GeminiErrorResponse::class.java)
            }.getOrNull()?.error?.message?.let { message ->
                return message
            }
        }

        return when (exception.code()) {
            400 -> "Bad request (400). Check your API key and try again."
            401, 403 -> "Invalid API key. Open toolbar → Set API Key and paste a valid key from Google AI Studio."
            404 -> "Model not found. Update the app or contact support."
            429 -> "Rate limit reached. Wait a moment and try again."
            else -> "HTTP ${exception.code()}: ${exception.message()}"
        }
    }
}
