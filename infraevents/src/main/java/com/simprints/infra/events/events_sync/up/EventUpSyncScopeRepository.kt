package com.simprints.infra.events.events_sync.up

import com.simprints.infra.events.events_sync.up.domain.EventUpSyncOperation
import com.simprints.infra.events.events_sync.up.domain.EventUpSyncScope

interface EventUpSyncScopeRepository {

    suspend fun getUpSyncScope(): EventUpSyncScope

    suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation)

    suspend fun deleteAll()
}
