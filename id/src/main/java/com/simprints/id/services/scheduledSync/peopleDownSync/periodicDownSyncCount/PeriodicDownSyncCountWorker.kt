package com.simprints.id.services.scheduledSync.peopleDownSync.periodicDownSyncCount

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncCountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import javax.inject.Inject

class PeriodicDownSyncCountWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    override fun doWork(): Result {

        injectDependencies()

        return try {
            val numberOfPeopleToDownSync = executeDownSyncCountTask()
            syncStatusDatabase.syncStatusModel.updatePeopleToDownSyncCount(numberOfPeopleToDownSync)

            if (numberOfPeopleToDownSync > 0) {
                peopleDownSyncMaster.schedule(preferencesManager.projectId, preferencesManager.userId)
            }

            Result.SUCCESS
        } catch (throwable: Throwable) {
            Result.RETRY
        }
    }

    private fun executeDownSyncCountTask() = PeopleDownSyncCountTask(remoteDbManager, dbManager,
        preferencesManager, loginInfoManager).execute().blockingGet()

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}
