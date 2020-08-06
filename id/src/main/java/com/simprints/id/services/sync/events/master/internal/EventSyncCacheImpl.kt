package com.simprints.id.services.sync.events.master.internal

import android.annotation.SuppressLint
import android.content.SharedPreferences
import java.util.*

@SuppressLint("ApplySharedPref")
class EventDownSyncCacheImpl(private val sharedForProgresses: SharedPreferences,
                             private val sharedForLastSyncTime: SharedPreferences) : EventDownSyncCache {


    override fun readLastSuccessfulSyncTime(): Date? {
        val dateLong = sharedForLastSyncTime.getLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, -1)
        return if (dateLong > -1) Date(dateLong) else null
    }

    override fun storeLastSuccessfulSyncTime(lastSyncTime: Date?) {
        sharedForLastSyncTime.edit().putLong(PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY, lastSyncTime?.time ?: -1).apply()
    }

    override fun readProgress(workerId: String): Int =
        (sharedForProgresses.getInt(workerId, 0))

    override fun saveProgress(workerId: String, progress: Int) {
        sharedForProgresses.edit().putInt(workerId, progress).commit()
    }

    override fun clearProgresses() {
        sharedForProgresses.edit().clear().commit()
    }


    companion object {
        const val PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY = "PEOPLE_SYNC_CACHE_LAST_SYNC_TIME_KEY"
    }
}
