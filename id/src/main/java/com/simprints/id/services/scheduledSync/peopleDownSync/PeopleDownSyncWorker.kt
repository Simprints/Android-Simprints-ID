package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import timber.log.Timber
import javax.inject.Inject

class PeopleDownSyncWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var localDbManager: LocalDbManager

    override fun doWork(): Result {

        injectDependencies()
        val task = PeopleDownSyncTask(remoteDbManager, dbManager, preferencesManager, loginInfoManager, localDbManager)

        return try {
            task.execute()
            Result.SUCCESS
        } catch (exception: TransientSyncFailureException) {
            Timber.e(exception)
            Result.RETRY
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.FAILURE
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}
