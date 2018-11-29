package com.simprints.id.services.scheduledSync.peopleDownSync.peopleCount

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncOption
import javax.inject.Inject

sealed class SyncCountWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster
    @Inject lateinit var analyticsManager: AnalyticsManager

    abstract val resultForErrors:Result

    override fun doWork(): Result {

        injectDependencies()
        return try {
            val numberOfPeopleToDownSync = executeDownSyncCountTask()
            syncStatusDatabase.syncStatusModel.updatePeopleToDownSyncCount(numberOfPeopleToDownSync)
            if (numberOfPeopleToDownSync > 0) {
                scheduleDownSyncWorkIfRequired(loginInfoManager.getSignedInProjectIdOrEmpty())
            }

            Result.SUCCESS
        } catch (throwable: Throwable) {
            analyticsManager.logThrowable(throwable)
            resultForErrors
        }
    }

    private fun executeDownSyncCountTask() =
        PeopleDownSyncCountTask(remoteDbManager, dbManager, preferencesManager, loginInfoManager)
            .execute().blockingGet()

    abstract fun scheduleDownSyncWorkIfRequired(projectId: String)

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }

    class PeriodicWorker: SyncCountWorker() {

        override val resultForErrors: Result = Result.RETRY
        override fun scheduleDownSyncWorkIfRequired(projectId: String) {
            peopleDownSyncMaster.schedule(loginInfoManager.getSignedInProjectIdOrEmpty())
        }
    }

    class OneTimeWorker: SyncCountWorker() {

        override val resultForErrors: Result = Result.FAILURE
        override fun scheduleDownSyncWorkIfRequired(projectId: String) {
            if (preferencesManager.peopleDownSyncOption == PeopleDownSyncOption.ACTIVE) {
                peopleDownSyncMaster.schedule(projectId)
            }
        }
    }
}

