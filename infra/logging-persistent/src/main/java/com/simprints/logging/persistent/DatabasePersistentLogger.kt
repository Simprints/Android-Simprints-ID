package com.simprints.logging.persistent

import com.simprints.logging.persistent.database.DbLogEntry
import com.simprints.logging.persistent.database.LogEntryDao
import com.simprints.logging.persistent.tools.ScopeProvider
import com.simprints.logging.persistent.tools.TimestampProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class DatabasePersistentLogger @Inject constructor(
    private val logDao: LogEntryDao,
    private val timestampProvider: TimestampProvider,
    private val scopeProvider: ScopeProvider,
) : PersistentLogger {
    override fun logSync(
        type: LogEntryType,
        title: String,
        body: String,
    ) {
        runBlocking(scopeProvider.dispatcherIO) {
            log(type, timestampProvider.nowMs(), title, body)
        }
    }

    override fun logSync(
        type: LogEntryType,
        timestampMs: Long,
        title: String,
        body: String,
    ) {
        runBlocking(scopeProvider.dispatcherIO) {
            log(type, timestampMs, title, body)
        }
    }

    override suspend fun log(
        type: LogEntryType,
        title: String,
        body: String,
    ) = log(type, timestampProvider.nowMs(), title, body)

    override suspend fun log(
        type: LogEntryType,
        timestampMs: Long,
        title: String,
        body: String,
    ) {
        // Running on external scope to log even if initial scope gets cancelled
        scopeProvider.externalScope.launch {
            with(logDao) {
                val now = timestampProvider.nowMs()

                save(
                    DbLogEntry(
                        expiresAtMs = now + EXPIRATION_PERIOD_MS,
                        timestampMs = timestampMs,
                        type = type.name,
                        title = title,
                        body = body,
                    ),
                )
                prune(now)
            }
        }
    }

    override suspend fun get(type: LogEntryType): List<LogEntry> = logDao
        .getByType(type.name)
        .map { fromDbEntry(it) }

    override suspend fun clear() {
        // Clearing on external scope to finish even if initial scope gets cancelled
        scopeProvider.externalScope.launch { logDao.deleteAll() }
    }

    private fun fromDbEntry(entry: DbLogEntry): LogEntry = LogEntry(
        timestampMs = entry.timestampMs,
        type = LogEntryType.valueOf(entry.type),
        title = entry.title,
        body = entry.body,
    )

    companion object {
        private const val EXPIRATION_PERIOD_MS = 14 * 24 * 60 * 60 * 1000 // 2 weeks in ms
    }
}
