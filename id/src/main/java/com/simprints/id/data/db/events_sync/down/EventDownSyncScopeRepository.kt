package com.simprints.id.data.db.events_sync.down

import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncScope

interface EventDownSyncScopeRepository {

    suspend fun getDownSyncScope(): EventDownSyncScope

    suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation)

    suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation

    suspend fun deleteAll()
}
