package com.simprints.logging.persistent.database

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "DbLogEntry",
)
@Keep
internal data class DbLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val expiresAtMs: Long,
    val timestampMs: Long,
    val type: String,
    val title: String,
    val body: String,
)
