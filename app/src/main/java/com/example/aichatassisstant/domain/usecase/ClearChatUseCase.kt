package com.example.aichatassisstant.domain.usecase

import com.example.aichatassisstant.domain.repository.ChatRepository
import javax.inject.Inject

class ClearChatUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    suspend operator fun invoke() = repository.clearAllMessages()
}
