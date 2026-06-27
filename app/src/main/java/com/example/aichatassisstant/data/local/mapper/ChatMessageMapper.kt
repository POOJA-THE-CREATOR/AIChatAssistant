package com.example.aichatassisstant.data.local.mapper

import com.example.aichatassisstant.data.local.entity.ChatMessageEntity
import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        content = content,
        role = if (isFromUser) MessageRole.USER else MessageRole.BOT,
        timestamp = timestamp
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        content = content,
        isFromUser = role == MessageRole.USER,
        timestamp = timestamp
    )
}

fun List<ChatMessageEntity>.toDomainList(): List<ChatMessage> {
    return map { it.toDomain() }
}
