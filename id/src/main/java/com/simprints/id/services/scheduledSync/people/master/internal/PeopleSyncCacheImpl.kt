package com.simprints.id.services.scheduledSync.people.master.internal

import android.annotation.SuppressLint
import android.content.SharedPreferences
import timber.log.Timber
import java.util.*

@SuppressLint("ApplySharedPref")
class PeopleSyncCacheImpl(private val sharedForProgresses: SharedPreferences,
                          private val sharedForLastSyncTime: SharedPreferences) : PeopleSyncCache {


    override fun readLastSuccessfulSyncTime(): Date? {
        val dateLong = sharedForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        return if (dateLong > -1) Date(dateLong) else null
    }

    override fun storeLastSuccessfulSyncTime(lastSyncTime: Date?) {
        sharedForLastSyncTime.edit().putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    override fun readProgress(workerId: String): Int =
        (sharedForProgresses.getInt(workerId, 0)).also {

            //TODO: Remove after tests
            Timber.d("I/SYNC - Cache - ${workerId.take(4)} $it")
        }

    override fun saveProgress(workerId: String, progress: Int) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()

        //TODO: Remove after tests
        Timber.d("I/SYNC - Cache - ${workerId.take(4)} $progress")
    }

    override fun clearProgresses() {
        sharedForProgresses.edit().clear().commit()
    }


    companion object {
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"
    }
}
