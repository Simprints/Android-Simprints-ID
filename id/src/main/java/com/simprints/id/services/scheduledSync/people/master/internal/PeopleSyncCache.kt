package com.simprints.id.services.scheduledSync.people.master.internal

import java.util.*

interface PeopleSyncCache {

    var lastSuccessfulSyncTime: Date?

    fun readProgress(workerId: String): Int
    fun saveProgress(workerId: String, progress: Int)
    fun clearProgresses()

    fun clearLastSyncTime()
}
