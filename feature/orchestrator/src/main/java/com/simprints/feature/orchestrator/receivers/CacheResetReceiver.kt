package com.simprints.feature.orchestrator.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.SessionCoroutineScope
import com.simprints.feature.orchestrator.cache.OrchestratorCache
import com.simprints.infra.events.session.SessionEventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Cached step structure might change between SID versions therefore caches should be cleared to avoid unmarshalling exceptions.
 * Since we do not support cross-version sessions, any ongoing scopes should be closed as well.
 */
@ExcludedFromGeneratedTestCoverageReports("Platform glue code")
@AndroidEntryPoint
internal class CacheResetReceiver : BroadcastReceiver() {
    @Inject
    @SessionCoroutineScope
    lateinit var externalScope: CoroutineScope

    @Inject
    lateinit var sessionEventRepository: SessionEventRepository

    @Inject
    lateinit var orchestratorCache: OrchestratorCache

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED == intent.action) {
            orchestratorCache.clearCache()
            externalScope.launch { sessionEventRepository.closeCurrentSession() }
        }
    }
}
