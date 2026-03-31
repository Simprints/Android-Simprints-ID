package com.simprints.feature.orchestrator.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.broadcasts.InternalBroadcaster
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Step cache should be cleared when session is closed to avoid mixing data between sessions.
 */
@ExcludedFromGeneratedTestCoverageReports("Platform glue code")
@AndroidEntryPoint
internal class CacheResetReceiver : BroadcastReceiver() {
    @Inject
    lateinit var orchestratorCache: OrchestratorCache

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (InternalBroadcaster.SESSION_CLOSED_ACTION == intent.action) {
            orchestratorCache.clearCache()
        }
    }
}
