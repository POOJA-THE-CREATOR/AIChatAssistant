package com.example.aichatassisstant.data.remote

import com.example.aichatassisstant.data.local.ApiKeyManager
import com.example.aichatassisstant.data.remote.api.GeminiApiService
import com.example.aichatassisstant.data.remote.dto.GeminiContent
import com.example.aichatassisstant.data.remote.dto.GeminiPart
import com.example.aichatassisstant.data.remote.dto.GeminiRequest
import com.example.aichatassisstant.data.remote.dto.GeminiResponse
import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRemoteDataSource @Inject constructor(
    private val apiService: GeminiApiService,
    private val apiKeyManager: ApiKeyManager,
    private val errorMapper: GeminiErrorMapper,
    private val demoChatDataSource: DemoChatDataSource
) {

    suspend fun generateResponse(messages: List<ChatMessage>): Result<String> {
        if (!apiKeyManager.isConfigured()) {
            return demoChatDataSource.generateResponse(messages)
        }

        val request = buildRequest(messages)
        var lastError: Exception? = null

        for (model in FALLBACK_MODELS) {
            try {
                val response = apiService.generateContent(
                    model = model,
                    apiKey = apiKeyManager.getApiKey(),
                    request = request
                )
                return Result.success(extractResponseText(response))
            } catch (exception: HttpException) {
                lastError = exception
                if (exception.code() == 404) continue
                return Result.failure(IllegalStateException(errorMapper.mapException(exception)))
            } catch (exception: Exception) {
                return Result.failure(IllegalStateException(errorMapper.mapException(exception)))
            }
        }

        val failure = lastError ?: IllegalStateException("No Gemini model available")
        return Result.failure(IllegalStateException(errorMapper.mapException(failure)))
    }

    private fun buildRequest(messages: List<ChatMessage>): GeminiRequest {
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

    private fun extractResponseText(response: GeminiResponse): String {
        response.promptFeedback?.blockReason?.let { reason ->
            throw IllegalStateException("Response blocked: $reason")
        }

        val candidate = response.candidates?.firstOrNull()
            ?: throw IllegalStateException("Gemini returned no response. Try again.")

        candidate.finishReason?.let { reason ->
            if (reason != "STOP" && reason != "MAX_TOKENS") {
                throw IllegalStateException("Response stopped: $reason")
            }
        }

        val text = candidate.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?.trim()

        if (text.isNullOrEmpty()) {
            throw IllegalStateException("Gemini returned an empty response. Try again.")
        }
        return text
    }

    companion object {
        private val FALLBACK_MODELS = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-2.0-flash-lite"
        )
    }
}
