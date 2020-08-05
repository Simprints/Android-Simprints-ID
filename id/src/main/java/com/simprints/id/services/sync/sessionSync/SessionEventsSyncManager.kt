package com.simprints.id.services.sync.sessionSync

interface SessionEventsSyncManager {
    fun scheduleSessionsSync()
    fun cancelSyncWorkers()
}
