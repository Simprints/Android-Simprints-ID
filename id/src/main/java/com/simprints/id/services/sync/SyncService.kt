package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.services.progress.notifications.NotificationBuilder
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.progress.service.ProgressTask

class SyncService : ProgressService<SyncTaskParameters>() {

    companion object {

        fun getClient(context: Context): SyncClient =
                SyncClient(context)
    }

    private val app: Application by lazy {
        (application as Application)
    }

    override fun getTask(taskParameters: SyncTaskParameters): ProgressTask =
            SyncTask(app.dataManager, taskParameters)

    override fun getProgressNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
            app.notificationFactory.syncProgressNotification()

    override fun getCompleteNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
            app.notificationFactory.syncCompleteNotification()

    override fun getErrorNotificationBuilder(taskParameters: SyncTaskParameters): NotificationBuilder =
            app.notificationFactory.syncErrorNotification()
}
