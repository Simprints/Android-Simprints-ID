package com.simprints.infra.eventsync.sync.common

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.domain.sync.SyncFailureReason
import com.simprints.core.domain.sync.SyncOutcome
import com.simprints.core.domain.sync.toSyncFailureReason
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("ApplySharedPref")
@Singleton
class EventSyncCache @Inject constructor(
    securityManager: SecurityManager,
    @param:DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val sharedForCounts =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS)
    private val sharedForProgresses =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_PROGRESSES_SHARED_PREFS)
    private val sharedForLastSyncTime =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)
    private val lastSyncOutcome = MutableStateFlow<SyncOutcome?>(null)

    /** The last attempt and how it turned out, then every later one. */
    fun observeLastSyncOutcome(): Flow<SyncOutcome?> =
        lastSyncOutcome.onStart { lastSyncOutcome.compareAndSet(null, readLastSyncOutcome()) }

    /** When event sync was last attempted and how it turned out; null when it never has been. */
    suspend fun readLastSyncOutcome(): SyncOutcome? = withContext(dispatcher) {
        val lastAttempt = sharedForLastSyncTime
            .getLong(LAST_ATTEMPT_TIME_KEY, -1)
            .takeIf { it >= 0 }
            ?.let { timestamp ->
                SyncOutcome(
                    timestamp = Timestamp(timestamp),
                    failure = sharedForLastSyncTime.getString(LAST_ATTEMPT_FAILURE_KEY, null)?.toSyncFailureReason(),
                )
            }
        lastAttempt ?: readLastSuccessfulSyncTimeBlocking()?.let { SyncOutcome(timestamp = it, failure = null) }
    }

    suspend fun readLastSuccessfulSyncTime(): Timestamp? = withContext(dispatcher) { readLastSuccessfulSyncTimeBlocking() }

    private fun readLastSuccessfulSyncTimeBlocking(): Timestamp? = sharedForLastSyncTime
        .getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        .takeIf { it >= 0 }
        ?.let { Timestamp(it) }

    /** Records an attempt: when it ended, and why it failed when it did. */
    suspend fun storeLastSyncOutcome(
        timestamp: Timestamp,
        failure: SyncFailureReason?,
    ): Unit = withContext(dispatcher) {
        sharedForLastSyncTime.edit {
            putLong(LAST_ATTEMPT_TIME_KEY, timestamp.ms)
            if (failure == null) {
                remove(LAST_ATTEMPT_FAILURE_KEY)
                putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, timestamp.ms)
            } else {
                putString(LAST_ATTEMPT_FAILURE_KEY, failure.name)
            }
        }
        lastSyncOutcome.value = SyncOutcome(timestamp, failure)
    }

    suspend fun clearLastSyncOutcome(): Unit = withContext(dispatcher) {
        sharedForLastSyncTime.edit {
            remove(LAST_ATTEMPT_TIME_KEY)
            remove(LAST_ATTEMPT_FAILURE_KEY)
            remove(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY)
        }
        lastSyncOutcome.value = null
    }

    suspend fun readProgress(workerId: String): Int = withContext(dispatcher) {
        sharedForProgresses.getInt(workerId, 0)
    }

    suspend fun saveProgress(
        workerId: String,
        progress: Int,
    ): Unit = withContext(dispatcher) {
        sharedForProgresses.edit(commit = true) { putInt(workerId, progress) }
    }

    suspend fun shouldIgnoreMax(): Boolean = withContext(dispatcher) {
        sharedForCounts.getBoolean(KEY_IGNORE_MAX, false)
    }

    suspend fun readMax(workerId: String): Int = withContext(dispatcher) {
        sharedForCounts.getInt(workerId, 0)
    }

    suspend fun saveMax(
        workerId: String,
        max: Int?,
    ): Unit = withContext(dispatcher) {
        sharedForCounts.edit(commit = true) {
            if (max == null) {
                putBoolean(KEY_IGNORE_MAX, true)
            } else {
                putInt(workerId, max)
            }
        }
    }

    suspend fun clearProgresses(): Unit = withContext(dispatcher) {
        // calling commit after clear sometimes throw SecurityException
        // it is a reported bug in Jetpack Security and not yet resolved.
        // https://issuetracker.google.com/issues/138314232#comment23
        // and https://issuetracker.google.com/issues/169904974
        try {
            sharedForProgresses.edit(commit = true) { clear() }
            sharedForCounts.edit(commit = true) { clear() }
        } catch (ex: SecurityException) {
            Simber.e("Crashed during event sync cleanup", ex, tag = SYNC)
        }
    }

    companion object {
        @VisibleForTesting
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"

        @VisibleForTesting
        const val LAST_ATTEMPT_TIME_KEY = "LAST_SYNC_ATTEMPT_TIME_KEY"

        @VisibleForTesting
        const val LAST_ATTEMPT_FAILURE_KEY = "LAST_SYNC_ATTEMPT_FAILURE_KEY"

        const val KEY_IGNORE_MAX = "IGNORE_MAX_VALUES"

        const val FILENAME_FOR_PROGRESSES_SHARED_PREFS = "CACHE_PROGRESSES"
        const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_SYNC_TIME"
        const val FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS = "CACHE_DOWN_COUNTS"
    }
}
