package com.example.aichatassisstant.data.repository

import com.example.aichatassisstant.data.local.dao.ChatMessageDao
import com.example.aichatassisstant.data.local.entity.ChatMessageEntity
import com.example.aichatassisstant.data.local.mapper.toDomainList
import com.example.aichatassisstant.data.remote.GeminiRemoteDataSource
import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole
import com.example.aichatassisstant.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val geminiRemoteDataSource: GeminiRemoteDataSource
) : ChatRepository {

    override fun observeMessages(): Flow<List<ChatMessage>> {
        return chatMessageDao.observeAll().map { entities -> entities.toDomainList() }
    }

    override suspend fun sendMessage(content: String): Result<Unit> {
        insertUserMessage(content)
        return fetchAndSaveBotResponse()
    }

    override suspend fun retryLastResponse(): Result<Unit> {
        return fetchAndSaveBotResponse()
    }

    override suspend fun clearAllMessages() {
        chatMessageDao.deleteAll()
    }

    private suspend fun insertUserMessage(content: String) {
        val entity = ChatMessageEntity(
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insert(entity)
    }

    private suspend fun fetchAndSaveBotResponse(): Result<Unit> {
        val history = chatMessageDao.getAll().toDomainList()
        if (history.isEmpty()) {
            return Result.failure(IllegalStateException("No messages to respond to"))
        }

        // Skip if the last message is already a bot reply (nothing to retry).
        if (history.last().role == MessageRole.BOT) {
            return Result.success(Unit)
        }

        return geminiRemoteDataSource.generateResponse(history).fold(
            onSuccess = { responseText ->
                saveBotMessage(responseText)
                Result.success(Unit)
            },
            onFailure = { exception ->
                Result.failure(exception)
            }
        )
    }

    private suspend fun saveBotMessage(content: String) {
        val entity = ChatMessageEntity(
            content = content,
            isFromUser = false,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insert(entity)
    }
}
