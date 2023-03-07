package com.simprints.infra.events.events_sync.down.temp

import android.app.Service
import android.content.Intent
import android.os.IBinder
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Simber.tag(SYNC.name).i("Reset downSync")
        externalScope.launch {
            downSyncScopeRepository.deleteAll()
            stopSelf()
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}
