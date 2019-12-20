package com.simprints.id.services.scheduledSync.people.master

interface PeopleSyncProgressCache {

    fun getProgress(workerId: String): Int
    fun setProgress(workerId: String, progress: Int)
    fun clear()
}
