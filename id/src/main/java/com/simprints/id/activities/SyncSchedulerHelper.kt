package com.simprints.id.activities

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.isPeopleDownSyncOff
import com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount.OneTimeDownSyncCountMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount.PeriodicDownSyncCountMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.shouldDownSyncScheduleInForeground
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var oneTimeDownSyncCountMaster: OneTimeDownSyncCountMaster
    @Inject lateinit var periodicDownSyncCountMaster: PeriodicDownSyncCountMaster
    @Inject lateinit var scheduledSessionsSyncManager: SessionEventsSyncManager
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster

    init {
        appComponent.inject(this)
    }

    fun scheduleBackgroundSyncs() {
        schedulePeopleDownSyncCountIfNotOff()
        scheduleSessionsSync()
    }

    fun schedulePeopleDownSyncIfNotOff() {
        if (!preferencesManager.peopleDownSyncOption.isPeopleDownSyncOff()) {
            peopleDownSyncMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }
    }

    fun startPeopleDownSyncIfAllowedFromForeground() {
        if (preferencesManager.peopleDownSyncOption.shouldDownSyncScheduleInForeground()) {
            oneTimeDownSyncCountMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }
    }

    fun startDownSyncCount() {
        oneTimeDownSyncCountMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty() )
    }

    fun cancelDownSyncWorkers() {
        oneTimeDownSyncCountMaster.cancelWorker(loginInfoManager.getSignedInProjectIdOrEmpty())
        periodicDownSyncCountMaster.cancelWorker(loginInfoManager.getSignedInProjectIdOrEmpty())
    }

    private fun schedulePeopleDownSyncCountIfNotOff() {
        if (!preferencesManager.peopleDownSyncOption.isPeopleDownSyncOff()) {
            periodicDownSyncCountMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }
    }

    private fun scheduleSessionsSync() {
        scheduledSessionsSyncManager.scheduleSessionsSync()
    }
}
