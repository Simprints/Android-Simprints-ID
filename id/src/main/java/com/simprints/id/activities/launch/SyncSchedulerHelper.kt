package com.simprints.id.activities.launch

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncOption
import com.simprints.id.services.scheduledSync.peopleDownSync.periodicDownSyncCount.PeriodicDownSyncCountMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount.OneTimeDownSyncCountMaster
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var oneTimeDownSyncCountMaster: OneTimeDownSyncCountMaster
    @Inject lateinit var periodicDownSyncCountMaster: PeriodicDownSyncCountMaster
    @Inject lateinit var scheduledSessionsSyncManager: SessionEventsSyncManager

    init {
        appComponent.inject(this)
    }

    fun scheduleSyncsAndStartPeopleSyncIfNecessary() {
        if (preferencesManager.peopleDownSyncOption == PeopleDownSyncOption.ACTIVE) {
            oneTimeDownSyncCountMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }

        schedulePeopleSyncIfNecessary()
        scheduleSessionsSyncIfNecessary()
    }

    private fun schedulePeopleSyncIfNecessary() {
        if (preferencesManager.peopleDownSyncOption == PeopleDownSyncOption.ACTIVE ||
            preferencesManager.peopleDownSyncOption == PeopleDownSyncOption.BACKGROUND) {
            periodicDownSyncCountMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }
    }

    private fun scheduleSessionsSyncIfNecessary() {
        scheduledSessionsSyncManager.scheduleSyncIfNecessary()
    }
}
