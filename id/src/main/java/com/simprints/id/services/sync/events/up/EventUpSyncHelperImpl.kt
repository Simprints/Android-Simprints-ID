package com.simprints.id.services.sync.events.up

import com.simprints.id.data.db.events_sync.up.domain.EventUpSyncOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

interface EventUpSyncHelperImpl {

    suspend fun upSync(scope: CoroutineScope,
                       operation: EventUpSyncOperation): ReceiveChannel<EventUpSyncOperation>
}
