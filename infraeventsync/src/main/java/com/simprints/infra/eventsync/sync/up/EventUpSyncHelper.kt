package com.simprints.infra.eventsync.sync.up

import com.simprints.infra.eventsync.status.up.domain.EventUpSyncOperation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface EventUpSyncHelper {

    suspend fun countForUpSync(operation: EventUpSyncOperation): Int

    fun upSync(scope: CoroutineScope, operation: EventUpSyncOperation): Flow<EventUpSyncProgress>
}
