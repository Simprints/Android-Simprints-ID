package com.simprints.id.services.scheduledSync

import com.simprints.id.data.db.people_sync.down.local.PeopleDownSyncDao
import com.simprints.id.data.db.people_sync.up.local.PeopleUpSyncDao
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager

class SyncSchedulerImpl(private val preferencesManager: PreferencesManager,
                        private val sessionEventsSyncManager: SessionEventsSyncManager,
                        private val peopleSyncManager: PeopleSyncManager,
                        private val peopleUpSyncDao: PeopleUpSyncDao,
                        private val peopleDownSyncDao: PeopleDownSyncDao) : SyncManager {


    override fun scheduleBackgroundSyncs() {
        sessionEventsSyncManager.scheduleSessionsSync()

        if (preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.PERIODIC_BACKGROUND] == true) {
            peopleSyncManager.scheduleSync()
        }
    }

    override fun cancelBackgroundSyncs() {
        sessionEventsSyncManager.cancelSyncWorkers()
        peopleSyncManager.cancelScheduledSync()
    }

    override fun deleteSyncHistory() {
        peopleUpSyncDao.deleteAll()
        peopleDownSyncDao.deleteAll()
    }
}
