package com.simprints.logging.persistent

data class LogEntry(
    val timestampMs: Long,
    val type: LogEntryType,
    val title: String,
    val body: String,
)
