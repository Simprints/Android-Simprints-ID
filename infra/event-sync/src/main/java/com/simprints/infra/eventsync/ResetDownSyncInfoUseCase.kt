package com.simprints.infra.eventsync

import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import javax.inject.Inject

class ResetDownSyncInfoUseCase @Inject internal constructor(
    private val commCareSyncCache: CommCareSyncCache,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
) {
    suspend operator fun invoke() {
        downSyncScopeRepository.deleteAll()
        commCareSyncCache.clearAllSyncedCases()
    }

}
