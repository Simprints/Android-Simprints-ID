package com.simprints.id.activities.launch

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import com.simprints.id.services.scheduledSync.sessionSync.ScheduledSessionsSyncManager
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var scheduledSessionsSyncManager: ScheduledSessionsSyncManager
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster

    init {
        appComponent.inject(this)
    }

    fun scheduleSyncsAndStartPeopleSyncIfNecessary() {
        if (preferencesManager.syncOnCallout) {
            peopleDownSyncMaster.schedule(preferencesManager.projectId, preferencesManager.userId)
        }

        schedulePeopleSyncIfNecessary()
        scheduleSessionsSyncIfNecessary()
    }

    private fun schedulePeopleSyncIfNecessary() {
        if (preferencesManager.scheduledBackgroundSync) {
            //TODO: Schedule periodic downsync worker
        }
    }

    private fun scheduleSessionsSyncIfNecessary() {
        scheduledSessionsSyncManager.scheduleSyncIfNecessary()
    }
}
