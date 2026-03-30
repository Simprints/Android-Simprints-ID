package com.simprints.feature.storage.alert

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI code")
internal class ShowStorageWarningNotificationUseCase @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    operator fun invoke() {
        val manager = NotificationManagerCompat.from(context)
        if (manager.areNotificationsEnabled()) {
            manager.ensureChannelExists()

            val notification = NotificationCompat
                .Builder(context, STORAGE_NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(IDR.string.storage_alert_title))
                .setContentText(context.getString(IDR.string.storage_alert_description))
                .setSmallIcon(IDR.drawable.ic_notification_default)
                .setContentIntent(createOpenAppIntent())
                .addAction(createStorageIntentAction())
                .setOnlyAlertOnce(true)
                .build()
            manager.notify(STORAGE_NOTIFICATION_ID, notification)
        }
    }

    private fun NotificationManagerCompat.ensureChannelExists() = createNotificationChannel(
        NotificationChannelCompat
            .Builder(
                STORAGE_NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_DEFAULT,
            ).setName(context.getString(IDR.string.storage_alert_channel_name))
            .build(),
    )

    private fun createOpenAppIntent(): PendingIntent? = context.packageManager
        .getLaunchIntentForPackage(context.packageName)
        ?.wrapInPendingIntent()

    private fun createStorageIntentAction(): NotificationCompat.Action = Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
        .wrapInPendingIntent()
        .let {
            NotificationCompat.Action
                .Builder(
                    IDR.drawable.ic_notification_default,
                    context.getString(IDR.string.storage_alert_settings_action),
                    it,
                ).build()
        }

    private fun Intent.wrapInPendingIntent(): PendingIntent =
        PendingIntent.getActivity(context, 0, this, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    private companion object {
        private const val STORAGE_NOTIFICATION_ID = 42
        private const val STORAGE_NOTIFICATION_CHANNEL_ID = "STORAGE_NOTIFICATION_CHANNEL"
    }
}
