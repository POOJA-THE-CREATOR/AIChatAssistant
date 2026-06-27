package com.example.aichatassisstant.domain.model

enum class MessageRole {
    USER,
    BOT
}

data class ChatMessage(
    val id: Long = 0L,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)
