package com.simprints.id.services.scheduledSync

interface SyncManager {

    fun scheduleBackgroundSyncs()
    fun cancelBackgroundSyncs()
    fun deleteSyncHistory()
}
