package com.simprints.infra.aichat.model

data class ChatSession(
    val id: String,
    val createdAtMs: Long,
    val messages: List<ChatMessage>,
)
