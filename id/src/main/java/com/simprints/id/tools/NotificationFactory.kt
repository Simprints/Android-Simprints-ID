package com.simprints.id.tools

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.simprints.id.R
import com.simprints.id.services.progress.notifications.ProgressNotificationBuilder
import com.simprints.id.services.progress.notifications.ResultNotificationBuilder
import org.jetbrains.anko.notificationManager


class NotificationFactory(private val context: Context) {

    companion object {

        private val channelId = "SimprintsID"
        private val channelName = "SimprintsID"
        private val channelDescription = "Notifications from SimprintsID"

        private val tag = "SimprintsID"
        private val title = "Simprints ID sync"
        private val icon = R.drawable.ic_progress_notification
        private val progressContentText = "Sync in progress."
        private val completeContentText = "Sync completed."
        private val errorContentText = "Sync failed."
    }

    private val notificationManager = context.notificationManager

    fun initSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initSyncNotificationChannelPostO()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initSyncNotificationChannelPostO() {
        val channel = NotificationChannel(channelId, channelName,
                NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = channelDescription
        notificationManager.createNotificationChannel(channel)
    }

    fun syncProgressNotification() =
            ProgressNotificationBuilder(notificationManager,
                    NotificationCompat.Builder(context, channelId),
                    tag,
                    title,
                    icon,
                    { progressContentText })

    fun syncResultNotification() =
            ResultNotificationBuilder(notificationManager,
                    NotificationCompat.Builder(context, channelId),
                    tag,
                    title,
                    icon,
                    completeContentText,
                    { errorContentText })
}
