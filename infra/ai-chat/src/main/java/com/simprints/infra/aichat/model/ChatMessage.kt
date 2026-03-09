package com.simprints.infra.aichat.model

enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM,
}

data class ChatMessage(
    val role: ChatRole,
    val content: String,
    val timestampMs: Long,
)
