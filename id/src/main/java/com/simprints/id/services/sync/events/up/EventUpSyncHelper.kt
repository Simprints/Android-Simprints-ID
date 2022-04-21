package com.simprints.id.services.sync.events.up

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface EventUpSyncHelper {

    suspend fun countForUpSync(operation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation): Int

    suspend fun upSync(
        scope: CoroutineScope,
        operation: com.simprints.eventsystem.events_sync.up.domain.EventUpSyncOperation
    ): Flow<EventUpSyncProgress>
}
