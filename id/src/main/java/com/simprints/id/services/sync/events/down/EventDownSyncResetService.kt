package com.simprints.id.services.sync.events.down

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
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
import kotlinx.coroutines.Job
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

    private var resetJob: Job? = null

    @Inject
    @ExternalScope
    lateinit var externalScope: CoroutineScope

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Simber.i("Reset down sync service started", tag = SYNC)
        resetJob = externalScope.launch {
            startForegroundIfNeeded()
            // Reset current downsync state
            eventSyncManager.resetDownSyncInfo()
            // Trigger a new sync
            syncOrchestrator.startEventSync()
        }
        resetJob?.invokeOnCompletion { stopSelf() }

        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onTimeout(
        startId: Int,
        fgsType: Int,
    ) {
        resetJob?.cancel()
        super.onTimeout(startId, fgsType)
    }

    private fun startForegroundIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                CHANNEL_ID,
                "Maintenance Service",
                NotificationManager.IMPORTANCE_LOW,
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)

            val notification = NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build()

            when {
                // In Android 15 dataSync type might be timed out/restricted
                // while shortService ensures that it will always be executed
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ->
                    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE)
                // Starting from Android 10 foreground services must declare type
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                    startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

                else -> startForeground(1, notification)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "EventDownSyncResetServiceChannelId"
    }
}
