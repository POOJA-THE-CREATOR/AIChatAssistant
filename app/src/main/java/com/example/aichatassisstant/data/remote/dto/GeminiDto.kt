package com.example.aichatassisstant.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val promptFeedback: GeminiPromptFeedback? = null
)

data class GeminiCandidate(
    val content: GeminiCandidateContent?,
    @SerializedName("finishReason")
    val finishReason: String? = null
)

data class GeminiCandidateContent(
    val parts: List<GeminiPart>?
)

data class GeminiPromptFeedback(
    @SerializedName("blockReason")
    val blockReason: String? = null
)

data class GeminiErrorResponse(
    val error: GeminiError?
)

data class GeminiError(
    val message: String?,
    @SerializedName("status")
    val status: String? = null
)
