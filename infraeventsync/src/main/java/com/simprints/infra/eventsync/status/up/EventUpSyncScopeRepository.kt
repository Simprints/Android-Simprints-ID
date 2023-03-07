package com.simprints.infra.eventsync.status.up

import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import com.simprints.infra.eventsync.status.up.domain.EventUpSyncScope

interface EventUpSyncScopeRepository {

    suspend fun getUpSyncScope(): EventUpSyncScope

    suspend fun insertOrUpdate(syncScopeOperation: EventUpSyncOperation)

    suspend fun deleteAll()
}
