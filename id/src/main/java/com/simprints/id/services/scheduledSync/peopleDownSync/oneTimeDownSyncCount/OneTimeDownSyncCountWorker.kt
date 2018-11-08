package com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncCountTask
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncMaster
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OneTimeDownSyncCountWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster

    override fun doWork(): Result {

        injectDependencies()
        executeDownSyncCountTask()
        return Result.SUCCESS
    }

    private fun executeDownSyncCountTask() {

        PeopleDownSyncCountTask(remoteDbManager, dbManager,
            preferencesManager, loginInfoManager).execute()
            .subscribeBy(
                onSuccess = {
                    Timber.d("Writing number of people to downsync in room")
                    syncStatusDatabase.syncStatusModel.updatePeopleToDownSyncCount(it)
                    if(it > 0) {
//                        peopleDownSyncMaster.schedule(preferencesManager.projectId,
//                            preferencesManager.userId)
                    }
                },
                onError = {

                }
            )
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}
