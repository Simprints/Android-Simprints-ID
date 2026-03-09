package com.simprints.infra.aichat.engine

import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatMessage
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for chat inference backends.
 * Implementations may use cloud APIs, on-device LLMs, or offline FAQ matching.
 */
interface ChatEngine {
    /**
     * Generate a response for the given conversation history and context.
     * Returns a [Flow] of partial response strings for streaming UI updates.
     */
    fun chat(
        messages: List<ChatMessage>,
        context: ChatContext,
    ): Flow<String>

    /**
     * Check whether this engine is currently available for use.
     */
    suspend fun isAvailable(): Boolean
}
