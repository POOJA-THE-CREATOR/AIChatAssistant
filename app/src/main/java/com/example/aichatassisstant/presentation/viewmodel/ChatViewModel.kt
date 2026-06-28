package com.example.aichatassisstant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aichatassisstant.data.local.ApiKeyManager
import com.example.aichatassisstant.domain.usecase.ClearChatUseCase
import com.example.aichatassisstant.domain.usecase.ObserveMessagesUseCase
import com.example.aichatassisstant.domain.usecase.RetryLastResponseUseCase
import com.example.aichatassisstant.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val observeMessagesUseCase: ObserveMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val retryLastResponseUseCase: RetryLastResponseUseCase,
    private val clearChatUseCase: ClearChatUseCase,
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
        refreshModeState()
    }

    fun sendMessage(content: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            setLoading(true)
            clearError()

            sendMessageUseCase(content).fold(
                onSuccess = { setLoading(false) },
                onFailure = { exception ->
                    setLoading(false)
                    setError(exception.message ?: "Failed to send message")
                }
            )
        }
    }

    fun retryLastResponse() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            setLoading(true)
            clearError()

            retryLastResponseUseCase().fold(
                onSuccess = { setLoading(false) },
                onFailure = { exception ->
                    setLoading(false)
                    setError(exception.message ?: "Failed to get response")
                }
            )
        }
    }

    fun saveApiKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) {
            setError("API key cannot be empty")
            return
        }

        apiKeyManager.saveApiKey(trimmed)
        refreshModeState()
        setShowApiKeyDialog(false)

        viewModelScope.launch {
            retryLastResponse()
        }
    }

    fun showApiKeyDialog() {
        setShowApiKeyDialog(true)
    }

    fun dismissApiKeyDialog() {
        setShowApiKeyDialog(false)
    }

    fun clearChat() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            clearChatUseCase()
            clearError()
        }
    }

    fun dismissError() {
        clearError()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            observeMessagesUseCase().collect { messages ->
                _uiState.update { current -> current.copy(messages = messages) }
            }
        }
    }

    private fun refreshModeState() {
        _uiState.update { current ->
            current.copy(isDemoMode = !apiKeyManager.isConfigured())
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { current -> current.copy(isLoading = isLoading) }
    }

    private fun setError(message: String) {
        _uiState.update { current -> current.copy(errorMessage = message) }
    }

    private fun clearError() {
        _uiState.update { current -> current.copy(errorMessage = null) }
    }

    private fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { current -> current.copy(showApiKeyDialog = show) }
    }
}
