package com.simprints.infra.aichat.engine

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatMessage
import com.simprints.infra.aichat.model.ChatRole
import com.simprints.infra.network.ConnectivityTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud-based [ChatEngine] powered by Firebase AI Logic (Vertex AI + Gemini).
 * Uses streaming inference for progressive response display.
 */
@Singleton
internal class FirebaseChatEngine @Inject constructor(
    private val knowledgeBaseLoader: KnowledgeBaseLoader,
    private val contextBuilder: ContextBuilder,
    private val connectivityTracker: ConnectivityTracker,
) : ChatEngine {

    private val generativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.vertexAI())
            .generativeModel(
                modelName = MODEL_NAME,
                generationConfig = generationConfig {
                    maxOutputTokens = MAX_OUTPUT_TOKENS
                    temperature = TEMPERATURE
                    topP = TOP_P
                },
                systemInstruction = content { text(buildSystemPrompt()) },
            )
    }

    override fun chat(
        messages: List<ChatMessage>,
        context: ChatContext,
    ): Flow<String> = flow {
        val history = messages.dropLast(1).map { msg ->
            content(role = msg.role.toGeminiRole()) { text(msg.content) }
        }
        val chat = generativeModel.startChat(history)

        val lastMessage = messages.lastOrNull() ?: return@flow
        val prompt = buildUserPrompt(lastMessage.content, context)

        chat.sendMessageStream(prompt).collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }

    override suspend fun isAvailable(): Boolean = connectivityTracker.isConnected()

    private fun buildSystemPrompt(): String = buildString {
        appendLine(SYSTEM_PREAMBLE)
        appendLine()
        appendLine(knowledgeBaseLoader.load())
    }

    private fun buildUserPrompt(userMessage: String, context: ChatContext): String {
        val dynamicContext = contextBuilder.build(context)
        return if (dynamicContext.isNotBlank()) {
            "$dynamicContext\n\n---\n\n**User question**: $userMessage"
        } else {
            userMessage
        }
    }

    companion object {
        private const val MODEL_NAME = "gemini-2.5-flash-lite"
        private const val MAX_OUTPUT_TOKENS = 1024
        private const val TEMPERATURE = 0.3f
        private const val TOP_P = 0.8f

        private const val SYSTEM_PREAMBLE = """
You are the Simprints ID Assistant, a helpful support chatbot for field workers using the Simprints ID biometric identification app.

Your role:
- Help users troubleshoot problems with the app
- Explain how features and workflows work
- Provide step-by-step guidance when users are stuck
- Answer questions about app settings and configuration

Guidelines:
- Be concise and practical — field workers need quick answers
- Use simple, non-technical language
- If you don't know the answer, say so honestly and suggest contacting support
- When the user's current screen/step context is available, use it to give targeted advice
- Never make up features or settings that don't exist in the app
"""
    }
}

private fun ChatRole.toGeminiRole(): String = when (this) {
    ChatRole.USER -> "user"
    ChatRole.ASSISTANT -> "model"
    ChatRole.SYSTEM -> "user"
}
