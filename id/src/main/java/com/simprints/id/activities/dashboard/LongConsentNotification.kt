package com.simprints.id.activities.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.simprints.id.R

class LongConsentNotification(val context: Context) {

    private val channelId: String
        get() = context.getString(R.string.notification_long_consent_channel_id)

    private val channelName: String
        get() = context.getString(R.string.notification_long_consent_channel_title)

    private val channelContentTitle: String
        get() = context.getString(R.string.notification_long_consent_channel_content_title)

    private val downloadProgress: String
        get() = context.getString(R.string.notification_long_consent_progress_download)

    private val builder = NotificationCompat.Builder(context, channelId).apply {
        priority = NotificationCompat.PRIORITY_DEFAULT
        setSmallIcon(R.drawable.ic_file_download_black_24dp)
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.getSystemService(NotificationManager::class.java).apply {
                createNotificationChannel(
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT))
            }
    }

    fun setNotification(language: String) {
        builder.setContentTitle(String.format(channelContentTitle, language))
        builder.setProgress(100, 0, false)
        notificationManager.notify(languageToId(language), builder.build())
    }

    fun updateNotification(language: String, progress: Int) {
        builder.setContentTitle(String.format(channelContentTitle, language))
        builder.setProgress(100, progress, false)
        builder.setContentText(String.format(downloadProgress, progress))
        notificationManager.notify(languageToId(language), builder.build())
    }

    fun failedNotification(language: String) = notificationManager.cancel(languageToId(language))

    fun completeNotification(language: String) {
        builder.setProgress(0, 0, false)
        builder.setContentText(context.getString(R.string.notification_long_consent_complete))
        notificationManager.notify(languageToId(language), builder.build())
    }

    private fun languageToId(language: String): Int = language.map { it.toInt() }.sum()

}
