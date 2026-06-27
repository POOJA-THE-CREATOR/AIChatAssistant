package com.example.aichatassisstant.presentation.viewmodel

import com.example.aichatassisstant.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
