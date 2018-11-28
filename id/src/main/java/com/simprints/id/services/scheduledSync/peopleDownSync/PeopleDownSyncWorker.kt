package com.simprints.id.services.scheduledSync.peopleDownSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.sync.room.SyncStatusDatabase
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import timber.log.Timber
import javax.inject.Inject

class PeopleDownSyncWorker: Worker() {

    @Inject lateinit var remoteDbManager: RemoteDbManager
    @Inject lateinit var dbManager: DbManager
    @Inject lateinit var loginInfoManager: LoginInfoManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase
    @Inject lateinit var analyticsManager: AnalyticsManager

    override fun doWork(): Result {

        injectDependencies()
        val task = PeopleDownSyncTask(remoteDbManager, dbManager, preferencesManager,
            loginInfoManager, localDbManager, syncStatusDatabase.syncStatusModel)

        return try {
            Timber.d("DownSync task...starting")
            task.execute()
            Timber.d("DownSync task successful")
            Result.SUCCESS
        } catch (throwable: Throwable) {
            analyticsManager.logThrowable(throwable)
            Timber.e(throwable)
            Timber.d("DownSync task failure")
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
