package com.simprints.infra.aichat

import com.simprints.infra.aichat.database.ChatDao
import com.simprints.infra.aichat.database.ChatMessageEntity
import com.simprints.infra.aichat.database.ChatSessionEntity
import com.simprints.infra.aichat.engine.ChatEngine
import com.simprints.infra.aichat.engine.ChatEngineSelector
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatMessage
import com.simprints.infra.aichat.model.ChatRole
import com.simprints.infra.aichat.model.ChatSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main entry point for the AI chat feature. Manages sessions, routes to the
 * appropriate engine, and persists conversation history.
 */
@Singleton
class ChatRepository @Inject internal constructor(
    private val chatDao: ChatDao,
    private val engineSelector: ChatEngineSelector,
) {
    /**
     * Send a message and get a streaming response.
     * The user message and the full assistant reply are persisted to the database.
     */
    fun sendMessage(
        sessionId: String,
        userMessage: String,
        context: ChatContext,
    ): Flow<String> {
        val now = System.currentTimeMillis()
        val userMsg = ChatMessage(
            role = ChatRole.USER,
            content = userMessage,
            timestampMs = now,
        )

        return flow {
            chatDao.insertSession(ChatSessionEntity(id = sessionId, createdAtMs = now))
            chatDao.insertMessage(userMsg.toEntity(sessionId))

            val history = chatDao.getMessages(sessionId).map { it.toDomain() }
            val engine = engineSelector.selectEngine()

            val responseBuilder = StringBuilder()
            engine.chat(history, context)
                .onCompletion {
                    if (responseBuilder.isNotEmpty()) {
                        val assistantMsg = ChatMessage(
                            role = ChatRole.ASSISTANT,
                            content = responseBuilder.toString(),
                            timestampMs = System.currentTimeMillis(),
                        )
                        chatDao.insertMessage(assistantMsg.toEntity(sessionId))
                    }
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)
                    emit(chunk)
                }
        }
    }

    suspend fun createSession(): String {
        val id = UUID.randomUUID().toString()
        chatDao.insertSession(
            ChatSessionEntity(id = id, createdAtMs = System.currentTimeMillis()),
        )
        return id
    }

    suspend fun getSession(sessionId: String): ChatSession? {
        val session = chatDao.getSession(sessionId) ?: return null
        val messages = chatDao.getMessages(sessionId).map { it.toDomain() }
        return ChatSession(
            id = session.id,
            createdAtMs = session.createdAtMs,
            messages = messages,
        )
    }

    suspend fun getAllSessions(): List<ChatSession> =
        chatDao.getAllSessions().map { session ->
            ChatSession(
                id = session.id,
                createdAtMs = session.createdAtMs,
                messages = chatDao.getMessages(session.id).map { it.toDomain() },
            )
        }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessages(sessionId)
        chatDao.deleteSession(sessionId)
    }

    suspend fun clearAllHistory() {
        chatDao.deleteAllMessages()
        chatDao.deleteAllSessions()
    }

    suspend fun getActiveEngine(): ChatEngine = engineSelector.selectEngine()
}

private fun ChatMessage.toEntity(sessionId: String) = ChatMessageEntity(
    sessionId = sessionId,
    role = role.name,
    content = content,
    timestampMs = timestampMs,
)

private fun ChatMessageEntity.toDomain() = ChatMessage(
    role = ChatRole.valueOf(role),
    content = content,
    timestampMs = timestampMs,
)
