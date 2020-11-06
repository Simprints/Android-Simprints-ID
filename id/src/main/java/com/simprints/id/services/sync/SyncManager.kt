package com.simprints.id.services.sync

interface SyncManager {
    fun scheduleBackgroundSyncs()
    fun cancelBackgroundSyncs()
}
