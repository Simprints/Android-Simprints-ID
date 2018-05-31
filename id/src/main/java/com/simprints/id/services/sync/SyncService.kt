package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.services.progress.notifications.NotificationBuilder
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.progress.service.ProgressTask
import com.simprints.id.tools.NotificationFactory
import javax.inject.Inject

class SyncService : ProgressService<SyncTaskParameters>() {

    @Inject lateinit var notificationFactory: NotificationFactory
    @Inject lateinit var dataManager: DataManager

    companion object {

        fun getClient(context: Context): SyncClient =
                SyncClient(context)
    }

    override fun onCreate() {
        super.onCreate()
        (application as Application).component.inject(this)
    }

    override fun getTask(taskParameters: SyncTaskParameters): ProgressTask =
            SyncTask(dataManager, taskParameters)

    override fun getProgressNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncProgressNotification()

    override fun getCompleteNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncCompleteNotification()

    override fun getErrorNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
        notificationFactory.syncErrorNotification()
}
