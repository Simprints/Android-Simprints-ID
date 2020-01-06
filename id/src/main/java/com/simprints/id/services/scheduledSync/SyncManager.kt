package com.simprints.id.services.scheduledSync

interface SyncManager {

    fun scheduleBackgroundSyncs()
    fun cancelBackgroundSyncs()
    suspend fun deleteLastSyncInfo()
}
