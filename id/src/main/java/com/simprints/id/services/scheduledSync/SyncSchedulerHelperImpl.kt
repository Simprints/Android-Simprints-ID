package com.simprints.id.services.scheduledSync

import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.DownSyncManager
import com.simprints.id.services.scheduledSync.peopleDownSync.models.PeopleDownSyncTrigger
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManager

class SyncSchedulerHelperImpl(val preferencesManager: PreferencesManager,
                              val loginInfoManager: LoginInfoManager,
                              private val sessionEventsSyncManager: SessionEventsSyncManager,
                              private val downSyncManager: DownSyncManager): SyncSchedulerHelper {


    override fun scheduleBackgroundSyncs() {
        if (preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.PERIODIC_BACKGROUND] == true) {
            scheduleDownSyncPeople()
            scheduleSessionsSync()
        }
    }

    // LaunchPresenter
    override fun startDownSyncOnLaunchIfPossible() {
        if (preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.ON_LAUNCH_CALLOUT] == true) {
            downSyncManager.enqueueOneTimeDownSyncMasterWorker()
        }
    }

    override fun startDownSyncOnUserActionIfPossible() {
        if (preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL] == true) {
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

    override fun isDownSyncManualTriggerOn(): Boolean =
        preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL] ?: false
}
