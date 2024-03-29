package com.simprints.id.services.sync.events.down

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.ExternalScope
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import com.simprints.infra.sync.SyncOrchestrator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

// This is implemented as a service so it can be invoked indirectly by class name and avoid a
// circular module dependency
@ExcludedFromGeneratedTestCoverageReports("Workaround for circular module dependency")
@AndroidEntryPoint
class EventDownSyncResetService : Service() {

    @Inject
    lateinit var eventSyncManager: EventSyncManager

    @Inject
    lateinit var syncOrchestrator: SyncOrchestrator

    @Inject
    @ExternalScope
    lateinit var externalScope: CoroutineScope

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Simber.tag(SYNC.name).i("Reset downSync")
        externalScope.launch {
            startForegroundIfNeeded()
            // Reset current downsync state
            eventSyncManager.resetDownSyncInfo()
            // Trigger a new sync
            syncOrchestrator.startEventSync()
            stopSelf()
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun startForegroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Maintenance Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()
            // if runtime >= Q then use FOREGROUND_SERVICE_TYPE_DATA_SYNC
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            } else {
                startForeground(1, notification)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "EventDownSyncResetServiceChannelId"
    }
}
