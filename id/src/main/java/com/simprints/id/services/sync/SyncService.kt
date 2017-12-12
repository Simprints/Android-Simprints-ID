package com.simprints.id.services.sync

import android.content.Context
import com.simprints.id.Application
import com.simprints.id.data.DataManager
import com.simprints.id.services.progress.notifications.ProgressNotificationBuilder
import com.simprints.id.services.progress.notifications.ResultNotificationBuilder
import com.simprints.id.services.progress.service.ProgressService
import com.simprints.id.services.progress.service.ProgressTask


class SyncService: ProgressService<SyncTaskParameters>() {

    companion object {

        fun getClient(context: Context): SyncClient =
                SyncClient(context)

    }

    private lateinit var dataManager: DataManager

    override fun onCreate() {
        super.onCreate()
        dataManager = (application as Application).dataManager
    }

    override fun getTask(taskParameters: SyncTaskParameters): ProgressTask =
            SyncTask(dataManager, taskParameters)

    override fun getProgressNotificationBuilder(taskParameters: SyncTaskParameters): ProgressNotificationBuilder =
            (application as Application).notificationFactory.syncProgressNotification()

    override fun getResultNotificationBuilder(taskParameters: SyncTaskParameters): ResultNotificationBuilder =
            (application as Application).notificationFactory.syncResultNotification()

}
