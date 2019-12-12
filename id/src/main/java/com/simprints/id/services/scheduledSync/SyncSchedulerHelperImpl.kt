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
            downSyncManager.sync()
        }
    }

    override fun startDownSyncOnUserActionIfPossible() {
        if (preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL] == true) {
            downSyncManager.sync()
        }
    }

    override fun cancelAllWorkers() {
        cancelDownSyncWorkers()
        cancelSessionsSyncWorker()
    }

    override fun cancelSessionsSyncWorker() {
        sessionEventsSyncManager.cancelSyncWorkers()
    }

    override fun cancelDownSyncWorkers() {
        downSyncManager.cancelScheduledSync()
    }

    private fun scheduleDownSyncPeople() {
        downSyncManager.scheduleSync()
    }

    private fun scheduleSessionsSync() {
        sessionEventsSyncManager.scheduleSessionsSync()
    }

    override fun isDownSyncManualTriggerOn(): Boolean =
        preferencesManager.peopleDownSyncTriggers[PeopleDownSyncTrigger.MANUAL] ?: false
}
