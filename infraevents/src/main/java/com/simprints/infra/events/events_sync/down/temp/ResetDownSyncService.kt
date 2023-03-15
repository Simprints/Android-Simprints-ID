package com.simprints.infra.events.events_sync.down.temp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.simprints.core.ExternalScope
import com.simprints.infra.events.events_sync.down.EventDownSyncScopeRepository
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
import com.simprints.infra.logging.Simber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO: This is a temporary workaround to avoid a circular module dependency until we
// extract syncing in a separate module
@AndroidEntryPoint
class ResetDownSyncService : Service() {

    @Inject
    lateinit var downSyncScopeRepository: EventDownSyncScopeRepository

    @Inject
    @ExternalScope
    lateinit var externalScope: CoroutineScope

//    override fun onCreate() {
//        super.onCreate()
//        startForegroundIfNeeded()
//    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Simber.tag(SYNC.name).i("Reset downSync")
        externalScope.launch {
            startForegroundIfNeeded()
            downSyncScopeRepository.deleteAll()
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
                "MyChannelId",
                "My Foreground Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            manager.createNotificationChannel(chan)

            val notificationBuilder = NotificationCompat.Builder(this, "MyChannelId")
            val notification: Notification = notificationBuilder
                .setContentTitle("App is running on foreground")
                .setPriority(NotificationManager.IMPORTANCE_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setChannelId("MyChannelId")
                .build()
            startForeground(1, notification)
        }
    }
}
