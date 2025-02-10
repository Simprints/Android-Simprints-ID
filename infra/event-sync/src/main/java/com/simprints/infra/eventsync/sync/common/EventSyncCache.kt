package com.simprints.infra.eventsync.sync.common

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.simprints.core.DispatcherIO
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@SuppressLint("ApplySharedPref")
@Singleton
internal class EventSyncCache @Inject constructor(
    securityManager: SecurityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) {
    private val sharedForCounts =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS)
    private val sharedForProgresses =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_PROGRESSES_SHARED_PREFS)
    private val sharedForLastSyncTime =
        securityManager.buildEncryptedSharedPreferences(FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)

    suspend fun readLastSuccessfulSyncTime(): Timestamp? = withContext(dispatcher) {
        sharedForLastSyncTime
            .getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
            .takeIf { it >= 0 }
            ?.let { Timestamp(it) }
    }

    suspend fun storeLastSuccessfulSyncTime(lastSyncTime: Timestamp?): Unit = withContext(dispatcher) {
        sharedForLastSyncTime
            .edit()
            .putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.ms ?: -1)
            .apply()
    }

    suspend fun readProgress(workerId: String): Int = withContext(dispatcher) {
        sharedForProgresses.getInt(workerId, 0)
    }

    suspend fun saveProgress(
        workerId: String,
        progress: Int,
    ): Unit = withContext(dispatcher) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()
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
            sharedForProgresses.edit().clear().commit()
            sharedForCounts.edit().clear().commit()
        } catch (ex: SecurityException) {
            Simber.e("Crashed during event sync cleanup", ex, tag = SYNC)
        }
    }

    companion object {
        @VisibleForTesting
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"

        const val KEY_IGNORE_MAX = "IGNORE_MAX_VALUES"

        const val FILENAME_FOR_PROGRESSES_SHARED_PREFS = "CACHE_PROGRESSES"
        const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_SYNC_TIME"
        const val FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS = "CACHE_DOWN_COUNTS"
    }
}
