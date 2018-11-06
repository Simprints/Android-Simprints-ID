package com.simprints.id.services.progress.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simprints.id.R
import com.simprints.id.services.progress.Progress
import com.simprints.id.services.progress.UploadProgress
import com.simprints.id.services.sync.SyncCategory
import org.jetbrains.anko.notificationManager

class NotificationFactory(private val context: Context) {

    private val channelId: String
        get() = context.getString(R.string.notification_channel_id)

    private val channelName: String
        get() = context.getString(R.string.notification_channel_name)

    private val channelDescription: String
        get() = context.getString(R.string.notification_channel_description)

    private val syncTag: String
        get() = context.getString(R.string.sync_notification_tag)

    private val syncTitle: String
        get() = context.getString(R.string.sync_notification_title)

    private val syncCompleteContent: String
        get() = context.getString(R.string.sync_complete_notification_content)

    private val syncErrorContent: String
        get() = context.getString(R.string.sync_error_notification_content)

    private val syncProgressIcon: Int = R.drawable.ic_syncing

    private val syncCompleteIcon: Int = R.drawable.ic_sync_success

    private val syncErrorIcon: Int = R.drawable.ic_sync_failed

    private val notificationManager = context.notificationManager

    fun initSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            initSyncNotificationChannelPostO()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun initSyncNotificationChannelPostO() {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = channelDescription
        notificationManager.createNotificationChannel(channel)
    }

    fun syncProgressNotification(syncCategory: SyncCategory?) =
        ProgressNotificationBuilder(notificationManager,
            NotificationCompat.Builder(context, channelId),
            syncTag,
            syncTitle,
            syncProgressIcon,
            syncCategory
        ) { progress ->
            formatProgressContent(progress)
        }

    private fun formatProgressContent(progress: Progress): String =
        when {
            isProgressZero(progress) ->
                context.getString(R.string.syncing_calculating)
            else -> {
                val messageRes = if (progress is UploadProgress) {
                    R.string.sync_progress_upload_notification_content
                } else {
                    R.string.sync_progress_download_notification_content
                }

                context.getString(messageRes, progress.currentValue, progress.maxValue)
            }
        }

    private fun isProgressZero(progress: Progress): Boolean =
        progress.currentValue == 0 && progress.maxValue == 0

    fun syncCompleteNotification(syncCategory: SyncCategory?): NotificationBuilder =
        CompleteNotificationBuilder(notificationManager,
            NotificationCompat.Builder(context, channelId),
            syncTag,
            syncTitle,
            syncCompleteIcon,
            syncCategory
        ) { syncCompleteContent }

    fun syncErrorNotification(syncCategory: SyncCategory?): NotificationBuilder =
        ErrorNotificationBuilder(notificationManager,
            NotificationCompat.Builder(context, channelId),
            syncTag,
            syncTitle,
            syncErrorIcon,
            syncCategory
        ) { syncErrorContent }
}
