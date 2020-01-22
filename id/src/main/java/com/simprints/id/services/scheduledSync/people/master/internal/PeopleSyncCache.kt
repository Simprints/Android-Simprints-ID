package com.simprints.id.services.scheduledSync.people.master.internal

import java.util.*

interface PeopleSyncCache {

    fun readLastSuccessfulSyncTime(): Date?
    fun storeLastSuccessfulSyncTime(lastSyncTime: Date?)

    fun readProgress(workerId: String): Int
    fun saveProgress(workerId: String, progress: Int)
    fun clearProgresses()
}
