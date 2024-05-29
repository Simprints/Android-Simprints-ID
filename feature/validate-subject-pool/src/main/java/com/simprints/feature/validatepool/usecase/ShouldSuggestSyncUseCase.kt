package com.simprints.feature.validatepool.usecase

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.eventsync.EventSyncManager
import javax.inject.Inject

internal class ShouldSuggestSyncUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val syncManager: EventSyncManager,
) {

    suspend operator fun invoke(): Boolean = syncManager
        .getLastSyncTime()
        ?.let { timeHelper.msBetweenNowAndTime(it.time) > SYNC_THRESHOLD_MS }
        ?: true

    companion object {

        // TODO use config instead
        private const val SYNC_THRESHOLD_MS = 24 * 60 * 60 * 1000L
    }
}
