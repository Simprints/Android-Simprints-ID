package com.simprints.infra.eventsync.sync.common

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@SuppressLint("ApplySharedPref")
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

    suspend fun readLastSuccessfulSyncTime(): Date? = withContext(dispatcher) {
        val dateLong = sharedForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        if (dateLong > -1) Date(dateLong) else null
    }

    suspend fun storeLastSuccessfulSyncTime(lastSyncTime: Date?): Unit = withContext(dispatcher) {
        sharedForLastSyncTime.edit()
            .putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    suspend fun readProgress(workerId: String): Int = withContext(dispatcher) {
        sharedForProgresses.getInt(workerId, 0)
    }

    suspend fun saveProgress(workerId: String, progress: Int): Unit = withContext(dispatcher) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()
    }

    suspend fun readMax(workerId: String): Int = withContext(dispatcher) {
        sharedForCounts.getInt(workerId, 0)
    }

    suspend fun saveMax(workerId: String, max: Int): Unit = withContext(dispatcher) {
        sharedForCounts.edit().putInt(workerId, max).commit()
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
            Simber.e(ex)
        }
    }

    companion object {
        @VisibleForTesting
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"

        const val FILENAME_FOR_PROGRESSES_SHARED_PREFS = "CACHE_PROGRESSES"
        const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_SYNC_TIME"
        const val FILENAME_FOR_DOWN_COUNTS_SHARED_PREFS = "CACHE_DOWN_COUNTS"
    }
}
