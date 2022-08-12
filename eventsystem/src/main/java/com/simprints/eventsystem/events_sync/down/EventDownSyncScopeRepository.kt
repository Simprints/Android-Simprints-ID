package com.simprints.eventsystem.events_sync.down

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modes
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncOperation
import com.simprints.eventsystem.events_sync.down.domain.EventDownSyncScope

interface EventDownSyncScopeRepository {

    suspend fun getDownSyncScope(
        modes: List<Modes>,
        selectedModuleIDs: List<String>,
        syncGroup: GROUP
    ): EventDownSyncScope

    suspend fun insertOrUpdate(syncScopeOperation: EventDownSyncOperation)

    suspend fun refreshState(syncScopeOperation: EventDownSyncOperation): EventDownSyncOperation

    suspend fun deleteOperations(moduleIds: List<String>, modes: List<Modes>)

    suspend fun deleteAll()
}
