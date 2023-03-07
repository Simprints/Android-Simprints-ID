package com.simprints.id.services.sync.events.down

import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.events.events_sync.down.domain.EventDownSyncOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface EventDownSyncHelper {

    suspend fun countForDownSync(operation: EventDownSyncOperation): List<EventCount>

    suspend fun downSync(
        scope: CoroutineScope,
        operation: EventDownSyncOperation
    ): ReceiveChannel<EventDownSyncProgress>

}
