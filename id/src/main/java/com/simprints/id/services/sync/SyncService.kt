package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.sync.SyncManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.progress.notifications.NotificationBuilder
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.progress.service.ProgressTask
import com.simprints.id.services.progress.notifications.NotificationFactory
import javax.inject.Inject

class SyncService : ProgressService<SyncTaskParameters>() {

    @Inject lateinit var notificationFactory: NotificationFactory
    @Inject lateinit var dbManager: DbManager

    companion object {

        var syncCategory: SyncCategory? = null

        fun getClient(context: Context): SyncClient =
                SyncClient(context)

        fun catchUpWithSyncServiceIfStillRunning(syncManager: SyncManager,
                                                 preferencesManager: PreferencesManager,
                                                 loginInfoManager: LoginInfoManager) {
            if (ProgressService.isRunning.get()) {
                // The "sync" happens only once at time on Service, no matters how many times we call "sync".
                // When "sync" is called, syncManager connect to the Service and syncManager either starts
                // the sync or catch with the Sync state.
                SyncService.syncCategory?.let {
                    syncManager.sync(SyncTaskParameters.build(
                        preferencesManager.syncGroup, preferencesManager.selectedModules, loginInfoManager), it)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        (application as Application).component.inject(this)
    }

    override fun getTask(taskParameters: SyncTaskParameters): ProgressTask =
            SyncTask(dbManager, taskParameters)

    override fun getProgressNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncProgressNotification(syncCategory)

    override fun getCompleteNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncCompleteNotification(syncCategory)

    override fun getErrorNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncErrorNotification(syncCategory)
}
