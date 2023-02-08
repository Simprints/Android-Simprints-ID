package com.simprints.id.services.sync.events.master.internal

import android.annotation.SuppressLint
import com.google.android.gms.common.util.VisibleForTesting
import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

// TODO move into its own module
@SuppressLint("ApplySharedPref")
class EventSyncCacheImpl @Inject constructor(
    securityManager: SecurityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : EventSyncCache, com.simprints.feature.dashboard.main.sync.EventSyncCache {

    private val sharedForProgresses =
        securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_PROGRESSES_SHARED_PREFS)
    private val sharedForLastSyncTime =
        securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)

    override suspend fun readLastSuccessfulSyncTime(): Date? = withContext(dispatcher) {
        val dateLong = sharedForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        if (dateLong > -1) Date(dateLong) else null
    }

    override suspend fun storeLastSuccessfulSyncTime(lastSyncTime: Date?): Unit = withContext(dispatcher) {
        sharedForLastSyncTime.edit()
            .putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    override suspend fun readProgress(workerId: String): Int = withContext(dispatcher) {
        sharedForProgresses.getInt(workerId, 0)
    }

    override suspend fun saveProgress(workerId: String, progress: Int): Unit = withContext(dispatcher) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()
    }

    override suspend fun clearProgresses(): Unit = withContext(dispatcher) {
        // calling commit after clear sometimes throw SecurityException
        // it is a reported bug in Jetpack Security and not yet resolved.
        // https://issuetracker.google.com/issues/138314232#comment23
        // and https://issuetracker.google.com/issues/169904974
        try {
            sharedForProgresses.edit().clear().commit()
        } catch (ex: SecurityException) {
            Simber.e(ex)
        }
    }


    companion object {
        @VisibleForTesting
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"
    }
}
