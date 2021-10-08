package com.simprints.id.services.config

interface RemoteConfigScheduler {
    fun syncNow()
    fun scheduleSync()
    fun cancelScheduledSync()
}

