package com.simprints.id.services.sync.events.down

import com.simprints.eventsystem.event.domain.EventCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface EventDownSyncHelper {

    suspend fun countForDownSync(operation: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation): List<EventCount>

    suspend fun downSync(scope: CoroutineScope,
                         operation: com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
    ): ReceiveChannel<EventDownSyncProgress>

}
