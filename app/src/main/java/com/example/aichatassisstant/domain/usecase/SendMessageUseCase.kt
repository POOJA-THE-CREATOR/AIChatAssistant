package com.example.aichatassisstant.domain.usecase

import com.example.aichatassisstant.domain.repository.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    suspend operator fun invoke(content: String): Result<Unit> {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        return repository.sendMessage(trimmed)
    }
}
