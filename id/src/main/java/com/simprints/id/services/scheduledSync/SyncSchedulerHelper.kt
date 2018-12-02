package com.simprints.id.services.scheduledSync

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.isDownSyncActiveOnLaunch
import com.simprints.id.services.scheduledSync.peopleDownSync.models.isDownSyncActiveOnUserAction
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager
import javax.inject.Inject

class SyncSchedulerHelper(appComponent: AppComponent) {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var scheduledSessionsSyncManager: SessionEventsSyncManager
    @Inject lateinit var syncScopesBuilder: SyncScopesBuilder

    private var master: DownSyncManager //StopShip: DI

    init {
        appComponent.inject(this)
        master = DownSyncManager(syncScopesBuilder)
    }

    fun scheduleBackgroundSyncs() {
        scheduleDownSyncPeople()
        scheduleSessionsSync()
    }

    //LaunchPresenter and DashboardPresenter.
    fun startDownSyncOnLaunchIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnLaunch()) {
            master.enqueueOneTimeDownSyncMasterWorker()
        }
    }

    fun startDownSyncOnUserActionIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnUserAction()) {
            master.enqueueOneTimeDownSyncMasterWorker()
        }
    }

    fun cancelDownSyncWorkers() {
        master.dequeueAllSyncWorker()
    }

    private fun scheduleDownSyncPeople() {
        master.enqueuePeriodicDownSyncMasterWorker()
    }

    private fun scheduleSessionsSync() {
        scheduledSessionsSyncManager.scheduleSessionsSync()
    }
}
