package com.simprints.id.services.scheduledSync.sessionSync

import androidx.work.PeriodicWorkRequest

interface ScheduledSessionsSyncManager {
    fun scheduleSyncIfNecessary(): PeriodicWorkRequest
}
