package com.simprints.id.services.scheduledSync.sessionSync

interface SessionEventsSyncManager {
    fun scheduleSessionsSync()
    fun cancelSyncWorkers()
}
