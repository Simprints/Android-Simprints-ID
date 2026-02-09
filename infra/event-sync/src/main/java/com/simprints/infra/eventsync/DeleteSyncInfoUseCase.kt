package com.simprints.infra.eventsync

import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.up.EventUpSyncScopeRepository
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import javax.inject.Inject

class DeleteSyncInfoUseCase @Inject internal constructor(
    private val commCareSyncCache: CommCareSyncCache,
    private val downSyncScopeRepository: EventDownSyncScopeRepository,
    private val eventSyncCache: EventSyncCache,
    private val upSyncScopeRepo: EventUpSyncScopeRepository,
) {
    suspend operator fun invoke() {
        downSyncScopeRepository.deleteAll()
        commCareSyncCache.clearAllSyncedCases()
        upSyncScopeRepo.deleteAll()
        eventSyncCache.clearProgresses()
        eventSyncCache.storeLastSuccessfulSyncTime(null)
    }
    
}
