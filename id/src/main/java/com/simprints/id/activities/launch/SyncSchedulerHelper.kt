package com.simprints.id.activities.launch

import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleSync.ScheduledPeopleSyncManager
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import com.simprints.id.services.sync.SyncCategory
import com.simprints.id.services.sync.SyncTaskParameters
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var scheduledPeopleSyncManager: ScheduledPeopleSyncManager
    @Inject lateinit var scheduledSessionsSyncManager: SessionEventsSyncManager

    init {
        appComponent.inject(this)
    }

    fun scheduleSyncsAndStartPeopleSyncIfNecessary() {
        if (preferencesManager.syncOnCallout) {
            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager), SyncCategory.AT_LAUNCH)
        }

        schedulePeopleSyncIfNecessary()
        scheduleSessionsSyncIfNecessary()
    }

    private fun schedulePeopleSyncIfNecessary() {
        if (preferencesManager.scheduledBackgroundSync) {
            scheduledPeopleSyncManager.scheduleSyncIfNecessary()
        } else {
            scheduledPeopleSyncManager.deleteSyncIfNecessary()
        }
    }

    private fun scheduleSessionsSyncIfNecessary() {
        scheduledSessionsSyncManager.scheduleSyncIfNecessary()
    }
}
