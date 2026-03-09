package com.simprints.infra.aichat.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ChatSessionEntity")
@Keep
internal data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val createdAtMs: Long,
)
