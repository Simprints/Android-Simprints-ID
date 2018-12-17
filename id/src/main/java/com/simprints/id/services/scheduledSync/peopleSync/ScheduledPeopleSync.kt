package com.simprints.id.services.scheduledSync.peopleSync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.simprints.id.Application
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncCategory
import com.simprints.id.services.sync.SyncTaskParameters
import timber.log.Timber
import javax.inject.Inject
import androidx.work.Result


class ScheduledPeopleSync(context : Context, params : WorkerParameters)
    : Worker(context, params) {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager



    override fun doWork(): Result {
        if (applicationContext is Application) {
            (applicationContext as Application).component.inject(this)

            Timber.e("ScheduledPeopleSync - doWork")

            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager), SyncCategory.SCHEDULED_BACKGROUND)

            return Result.success()
        }
        return Result.failure()
    }
}
