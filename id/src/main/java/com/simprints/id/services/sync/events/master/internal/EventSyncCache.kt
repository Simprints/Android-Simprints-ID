package com.simprints.id.services.sync.events.master.internal

import java.util.*

interface EventSyncCache {

    companion object {
        const val FILENAME_FOR_PROGRESSES_SHARED_PREFS = "CACHE_PROGRESSES"
        const val FILENAME_FOR_LAST_SYNC_TIME_SHARED_PREFS = "CACHE_LAST_SYNC_TIME"
    }

    suspend fun readLastSuccessfulSyncTime(): Date?
    suspend fun storeLastSuccessfulSyncTime(lastSyncTime: Date?)

    suspend fun readProgress(workerId: String): Int
    suspend fun saveProgress(workerId: String, progress: Int)
    suspend fun clearProgresses()
}
