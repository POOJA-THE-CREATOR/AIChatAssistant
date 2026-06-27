package com.example.aichatassisstant.domain.repository

import com.example.aichatassisstant.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun observeMessages(): Flow<List<ChatMessage>>

    suspend fun sendMessage(content: String): Result<Unit>

    suspend fun retryLastResponse(): Result<Unit>

    suspend fun clearAllMessages()
}
