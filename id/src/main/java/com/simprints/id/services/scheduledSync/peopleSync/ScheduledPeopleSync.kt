package com.simprints.id.services.scheduledSync.peopleSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncCategory
import com.simprints.id.services.sync.SyncTaskParameters
import timber.log.Timber
import javax.inject.Inject

class ScheduledPeopleSync : Worker() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager

    override fun doWork(): Result {
        if (applicationContext is Application) {
            (applicationContext as Application).component.inject(this)

            Timber.e("ScheduledPeopleSync - doWork")

            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.selectedModules, loginInfoManager), SyncCategory.SCHEDULED_BACKGROUND)

            return Result.SUCCESS
        }
        return Result.FAILURE
    }
}
