package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.exceptions.safe.sync.TransientSyncFailureException
import timber.log.Timber
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class PeopleDownSyncWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    override fun doWork(): Result {

        injectDependencies()
        val task = PeopleDownSyncTask(remoteDbManager, dbManager, preferencesManager,
            loginInfoManager, localDbManager, syncStatusDatabase)

        return try {
            Timber.d("DownSync task...starting")
            task.execute()
            Timber.d("DownSync task successful")
            Result.SUCCESS
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Timber.d("DownSync task failure: Retry")
            Result.RETRY
        }
    }

    private fun injectDependencies() {
        val context = applicationContext
        if (context is Application) {
            context.component.inject(this)
        }
    }
}
