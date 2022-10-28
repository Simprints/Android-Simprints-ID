package com.simprints.id.services.sync.events.master.internal

import android.annotation.SuppressLint
import com.google.android.gms.common.util.VisibleForTesting
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import java.util.*
import javax.inject.Inject

@SuppressLint("ApplySharedPref")
class EventSyncCacheImpl @Inject constructor(securityManager: SecurityManager) : EventSyncCache {

    private val sharedForProgresses =
        securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_PROGRESSES_SHARED_PREFS)
    private val sharedForLastSyncTime =
        securityManager.buildEncryptedSharedPreferences(EventSyncCache.FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS)

    override fun readLastSuccessfulSyncTime(): Date? {
        val dateLong = sharedForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        return if (dateLong > -1) Date(dateLong) else null
    }

    override fun storeLastSuccessfulSyncTime(lastSyncTime: Date?) {
        sharedForLastSyncTime.edit()
            .putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    override fun readProgress(workerId: String): Int =
        sharedForProgresses.getInt(workerId, 0)

    override fun saveProgress(workerId: String, progress: Int) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()
    }

    override fun clearProgresses() {
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
