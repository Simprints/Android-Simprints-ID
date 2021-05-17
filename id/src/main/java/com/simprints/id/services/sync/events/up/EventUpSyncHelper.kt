package com.simprints.id.services.sync.events.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface EventUpSyncHelper {

    suspend fun countForUpSync(operation: EventUpSyncOperation): Int

    suspend fun upSync(
        scope: CoroutineScope,
        operation: EventUpSyncOperation
    ): Flow<EventUpSyncProgress>
}
