package com.simprints.id.services.scheduledSync

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.isDownSyncActiveOnLaunch
import com.simprints.id.services.scheduledSync.peopleDownSync.models.isDownSyncActiveOnUserAction
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager

class SyncSchedulerHelperImpl(val preferencesManager: PreferencesManager,
                              val loginInfoManager: LoginInfoManager,
                              private val sessionEventsSyncManager: SessionEventsSyncManager,
                              private val downSyncManager: DownSyncManager): SyncSchedulerHelper {


    override fun scheduleBackgroundSyncs() {
        scheduleDownSyncPeople()
        scheduleSessionsSync()
    }

    //LaunchPresenter and DashboardPresenter.
    override fun startDownSyncOnLaunchIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnLaunch()) {
            downSyncManager.enqueueOneTimeDownSyncMasterWorker()
        }
    }

    override fun startDownSyncOnUserActionIfPossible() {
        if (preferencesManager.peopleDownSyncOption.isDownSyncActiveOnUserAction()) {
            downSyncManager.enqueueOneTimeDownSyncMasterWorker()
        }
    }

    override fun cancelDownSyncWorkers() {
        downSyncManager.dequeueAllSyncWorker()
    }

    private fun scheduleDownSyncPeople() {
        downSyncManager.enqueuePeriodicDownSyncMasterWorker()
    }

    private fun scheduleSessionsSync() {
        sessionEventsSyncManager.scheduleSessionsSync()
    }
}
