package com.simprints.id.services.scheduledSync.people.master.internal

import java.util.*

interface PeopleSyncCache {

    companion object {
        const val FILENAME_FOR_PROGRESSES_SHARED_PREFS = "CACHE_PROGRESSES"
        const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_SYNC_TIME"
    }

    fun readLastSuccessfulSyncTime(): Date?
    fun storeLastSuccessfulSyncTime(lastSyncTime: Date?)

    fun readProgress(workerId: String): Int
    fun saveProgress(workerId: String, progress: Int)
    fun clearProgresses()
}
