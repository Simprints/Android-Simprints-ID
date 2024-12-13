package com.simprints.logging.persistent

/**
 * Log a basic data entry of the provided type.
 * Entries are stored for around 2 weeks and can be deleted at any point after that.
 */
interface PersistentLogger {
    fun logSync(
        type: LogEntryType,
        title: String,
        body: String,
    )

    fun logSync(
        type: LogEntryType,
        timestampMs: Long,
        title: String,
        body: String,
    )

    suspend fun log(
        type: LogEntryType,
        title: String,
        body: String,
    )

    suspend fun log(
        type: LogEntryType,
        timestampMs: Long,
        title: String,
        body: String,
    )

    /**
     * Get all entries of the provided type newest to oldest.
     */
    suspend fun get(type: LogEntryType): List<LogEntry>

    /**
     * Completely remove all logged entries of all types.
     */
    suspend fun clear()
}
