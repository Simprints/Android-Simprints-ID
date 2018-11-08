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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class PeriodicDownSyncCountWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var peopleDownSyncMaster: PeopleDownSyncMaster

    override fun doWork(): Result {

        injectDependencies()

        val task = PeopleDownSyncCountTask(remoteDbManager, dbManager,
            preferencesManager, loginInfoManager)

        task.execute()
            .subscribeBy(
                onSuccess = {
                    if (it > 0) {
                        peopleDownSyncMaster.schedule(preferencesManager.projectId, preferencesManager.userId)
                    }
                },
                onError = {

                }
            )
        return Result.SUCCESS
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}
