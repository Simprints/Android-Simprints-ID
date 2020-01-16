package com.simprints.id.services.scheduledSync

import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.people_sync.up.PeopleUpSyncScopeRepository
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncScheduler
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import timber.log.Timber

class SyncSchedulerImpl(private val preferencesManager: PreferencesManager,
                        private val sessionEventsSyncManager: SessionEventsSyncManager,
                        private val peopleSyncManager: PeopleSyncManager,
                        private val peopleUpSyncScopeRepository: PeopleUpSyncScopeRepository,
                        private val peopleDownSyncScopeRepository: PeopleDownSyncScopeRepository,
                        private val imageUpSyncScheduler: ImageUpSyncScheduler) : SyncManager {

    override fun scheduleBackgroundSyncs() {
        Timber.d("Background schedules synced")
        sessionEventsSyncManager.scheduleSessionsSync()
        peopleSyncManager.scheduleSync()
        imageUpSyncScheduler.scheduleImageUpSync()
    }

    override fun cancelBackgroundSyncs() {
        Timber.d("Background schedules canceled")
        sessionEventsSyncManager.cancelSyncWorkers()
        peopleSyncManager.cancelScheduledSync()
        imageUpSyncScheduler.cancelImageUpSync()
    }

    override suspend fun deleteLastSyncInfo() {
        peopleUpSyncScopeRepository.deleteAll()
        peopleDownSyncScopeRepository.deleteAll()
    }
}
