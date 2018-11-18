package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.PeriodicWorkRequest

interface SessionEventsSyncManager {
    fun scheduleSyncIfNecessary(): PeriodicWorkRequest
    fun cancelSyncWorkers()
}
