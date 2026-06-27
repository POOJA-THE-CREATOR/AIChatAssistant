package com.example.aichatassisstant.data.remote

import com.example.aichatassisstant.BuildConfig
import com.example.aichatassisstant.data.remote.api.GeminiApiService
import com.example.aichatassisstant.data.remote.dto.GeminiContent
import com.example.aichatassisstant.data.remote.dto.GeminiPart
import com.example.aichatassisstant.data.remote.dto.GeminiRequest
import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRemoteDataSource @Inject constructor(
    private val apiService: GeminiApiService
) {

    suspend fun generateResponse(messages: List<ChatMessage>): Result<String> {
        if (!GeminiErrorMapper.isApiKeyConfigured()) {
            return Result.failure(
                IllegalStateException(GeminiErrorMapper.mapException(IllegalStateException()))
            )
        }

        return try {
            val request = buildRequest(messages)
            val response = apiService.generateContent(
                model = BuildConfig.GEMINI_MODEL,
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )
            val text = extractResponseText(response)
            Result.success(text)
        } catch (exception: Exception) {
            Result.failure(IllegalStateException(GeminiErrorMapper.mapException(exception)))
        }
    }

    private fun buildRequest(messages: List<ChatMessage>): GeminiRequest {
        // Gemini requires alternating user/model turns; merge consecutive same-role messages.
        val contents = mutableListOf<GeminiContent>()
        for (message in messages) {
            val role = if (message.role == MessageRole.USER) "user" else "model"
            val last = contents.lastOrNull()
            if (last != null && last.role == role) {
                val mergedText = last.parts.first().text + "\n" + message.content
                contents[contents.lastIndex] = last.copy(
                    parts = listOf(GeminiPart(text = mergedText))
                )
            } else {
                contents.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = message.content))
                    )
                )
            }
        }
        return GeminiRequest(contents = contents)
    }

    private fun extractResponseText(response: com.example.aichatassisstant.data.remote.dto.GeminiResponse): String {
        val text = response.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()

        if (text.isNullOrEmpty()) {
            throw IllegalStateException("Gemini returned an empty response")
        }
        return text
    }
}
