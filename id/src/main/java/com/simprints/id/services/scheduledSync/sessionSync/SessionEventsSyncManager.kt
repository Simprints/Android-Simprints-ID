package com.simprints.id.services.scheduledSync.sessionSync

interface SessionEventsSyncManager {
    fun scheduleSyncIfNecessary()
    fun cancelSyncWorkers()
}
