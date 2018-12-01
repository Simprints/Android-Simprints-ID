package com.simprints.id.activities

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.isDownSyncActiveOnLaunch
import com.simprints.id.services.scheduledSync.peopleDownSync.isDownSyncActiveOnUserAction
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.newplan.controllers.MasterSync
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var scheduledSessionsSyncManager: SessionEventsSyncManager
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    private var master: MasterSync //StopShip: DI

    init {
        appComponent.inject(this)
        master = MasterSync(syncScopesBuilder)
    }

    fun scheduleBackgroundSyncs() {
        scheduleDownSyncPeople()
        scheduleSessionsSync()
    }

    //LaunchPresenter and DashboardPresenter.
    fun startDownSyncOnLaunchIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnLaunch()) {
            master.enqueueOneTimeSyncWorker()
        }
    }

    fun startDownSyncOnUserActionIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnUserAction()) {
            master.enqueueOneTimeSyncWorker()
        }
    }

    fun cancelDownSyncWorkers() {
        master.dequeueAllSyncWorker()
    }

    private fun scheduleDownSyncPeople() {
        master.enqueuePeriodicSyncWorker()
    }

    private fun scheduleSessionsSync() {
        scheduledSessionsSyncManager.scheduleSessionsSync()
    }
}
