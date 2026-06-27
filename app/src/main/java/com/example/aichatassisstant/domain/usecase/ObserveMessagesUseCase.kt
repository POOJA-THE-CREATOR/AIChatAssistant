package com.example.aichatassisstant.domain.usecase

import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    operator fun invoke(): Flow<List<ChatMessage>> = repository.observeMessages()
}
