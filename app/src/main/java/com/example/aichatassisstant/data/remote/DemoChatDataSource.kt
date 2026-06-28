package com.example.aichatassisstant.data.remote

import com.example.aichatassisstant.domain.model.ChatMessage
import com.example.aichatassisstant.domain.model.MessageRole
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DemoChatDataSource @Inject constructor() {

    suspend fun generateResponse(messages: List<ChatMessage>): Result<String> {
        delay(SIMULATED_NETWORK_DELAY_MS)

        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }?.content?.trim()
            ?: return Result.failure(IllegalStateException("No user message to respond to"))

        return Result.success(buildDemoReply(lastUserMessage, messages))
    }

    private fun buildDemoReply(userMessage: String, history: List<ChatMessage>): String {
        val lower = userMessage.lowercase()
        val botTurnCount = history.count { it.role == MessageRole.BOT }

        return when {
            lower.contains("hello") || lower.contains("hi") ->
                "Hello! I'm running in demo mode (no Gemini API key). I can still chat with canned replies. Ask me anything!"

            lower.contains("help") ->
                "Demo mode tips:\n• Type any message and I'll reply\n• For real AI, tap the lock icon and add a free key from Google AI Studio\n• Your chat history is still saved locally"

            lower.contains("who are you") || lower.contains("what are you") ->
                "I'm a demo assistant built into this app. Without a Gemini API key, I use local mock responses so you can test the UI."

            lower.contains("bye") || lower.contains("goodbye") ->
                "Goodbye! Add a Gemini API key anytime to switch to real AI responses."

            lower.endsWith("?") ->
                "That's a great question about \"$userMessage\". In demo mode I can't look things up, but with a free Gemini key I'd give you a real answer."

            botTurnCount == 0 ->
                "You said: \"$userMessage\"\n\nThis is a demo reply — no API key needed. Get a free key at Google AI Studio to enable real Gemini responses."

            else -> DEMO_REPLIES[Random.nextInt(DEMO_REPLIES.size)].format(userMessage)
        }
    }

    companion object {
        private const val SIMULATED_NETWORK_DELAY_MS = 900L

        private val DEMO_REPLIES = listOf(
            "Demo mode: I received \"%1\$s\". Connect a Gemini API key for intelligent replies.",
            "Thanks for your message! \"%1\$s\" — I'm a placeholder bot until you add an API key.",
            "Got it — \"%1\$s\". This app works fully in demo mode for testing the chat UI.",
            "Interesting point about \"%1\$s\". Enable Gemini via the toolbar lock icon for real AI."
        )
    }
}
