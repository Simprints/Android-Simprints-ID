package com.simprints.feature.chatbot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.chatbot.analytics.ChatbotAnalytics
import com.simprints.feature.chatbot.context.ChatContextProvider
import com.simprints.infra.aichat.ChatRepository
import com.simprints.infra.aichat.model.ChatMessage
import com.simprints.infra.aichat.model.ChatRole
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class ChatbotViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val connectivityTracker: ConnectivityTracker,
    private val contextProvider: ChatContextProvider,
    private val analytics: ChatbotAnalytics,
) : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isOffline = MutableLiveData(false)
    val isOffline: LiveData<Boolean> = _isOffline

    private var sessionId: String = UUID.randomUUID().toString()
    private var messageCount = 0
    private val sessionStartMs = System.currentTimeMillis()

    init {
        addWelcomeMessage()
        val isOnline = connectivityTracker.isConnected()
        analytics.trackChatOpened(isOnline)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val isOnline = connectivityTracker.isConnected()
        _isLoading.value = true
        _isOffline.value = !isOnline

        val userMsg = ChatMessage(
            role = ChatRole.USER,
            content = text,
            timestampMs = System.currentTimeMillis(),
        )
        appendMessage(userMsg)

        analytics.trackMessageSent()
        if (!isOnline) analytics.trackOfflineFallback()

        val sendStartMs = System.currentTimeMillis()
        messageCount++

        viewModelScope.launch {
            val context = contextProvider.buildContext()
            val responseBuilder = StringBuilder()
            chatRepository.sendMessage(sessionId, text, context)
                .catch { e ->
                    analytics.trackError(e.message ?: "Unknown error")
                    val errorMsg = ChatMessage(
                        role = ChatRole.ASSISTANT,
                        content = "Sorry, I encountered an error. Please try again.",
                        timestampMs = System.currentTimeMillis(),
                    )
                    appendMessage(errorMsg)
                    _isLoading.value = false
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)
                    updateStreamingResponse(responseBuilder.toString())
                }
            _isLoading.value = false
            analytics.trackResponseReceived(
                responseTimeMs = System.currentTimeMillis() - sendStartMs,
                isCloud = isOnline,
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            chatRepository.clearAllHistory()
            _messages.value = emptyList()
            sessionId = UUID.randomUUID().toString()
            messageCount = 0
            addWelcomeMessage()
        }
    }

    override fun onCleared() {
        super.onCleared()
        analytics.trackSessionClosed(
            messageCount = messageCount,
            durationMs = System.currentTimeMillis() - sessionStartMs,
        )
    }

    private fun addWelcomeMessage() {
        val welcome = ChatMessage(
            role = ChatRole.ASSISTANT,
            content = "Hi! I'm the Simprints Assistant. How can I help you today?",
            timestampMs = System.currentTimeMillis(),
        )
        _messages.value = listOf(welcome)
    }

    private fun appendMessage(message: ChatMessage) {
        _messages.value = (_messages.value.orEmpty()) + message
    }

    private fun updateStreamingResponse(currentText: String) {
        val current = _messages.value.orEmpty().toMutableList()

        val streamingMsg = ChatMessage(
            role = ChatRole.ASSISTANT,
            content = currentText,
            timestampMs = System.currentTimeMillis(),
        )

        if (current.last().role == ChatRole.ASSISTANT) {
            current[current.lastIndex] = streamingMsg
        } else {
            current.add(streamingMsg)
        }
        _messages.value = current
    }
}
