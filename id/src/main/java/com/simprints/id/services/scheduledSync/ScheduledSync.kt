package com.simprints.id.services.scheduledSync

import androidx.work.Worker
import com.simprints.id.Application
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.SyncCategory
import com.simprints.id.services.sync.SyncTaskParameters
import javax.inject.Inject


class ScheduledSync : Worker() {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var loginInfoManager: LoginInfoManager

    override fun doWork(): Result {
        if (applicationContext is Application) {
            (applicationContext as Application).component.inject(this)

            syncManager.sync(SyncTaskParameters.build(preferencesManager.syncGroup, preferencesManager.moduleId, loginInfoManager), SyncCategory.USER_INITIATED)

            return Result.SUCCESS
        }
        return Result.FAILURE
    }
}
