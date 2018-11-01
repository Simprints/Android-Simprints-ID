package com.simprints.id.services.scheduledSync.peopleDownSync.oneTimeDownSyncCount

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncCountTask
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class OneTimeDownSyncCountWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager

    override fun doWork(): Result {

        injectDependencies()

        val task = PeopleDownSyncCountTask(remoteDbManager, dbManager,
            preferencesManager, loginInfoManager)

        task.execute()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
            onSuccess = {
                //TODO: Update in room
                //TODO: Enqueue PeopleDownSyncWorker conditional upon design spec
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
