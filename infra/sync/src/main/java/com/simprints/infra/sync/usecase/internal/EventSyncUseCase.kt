package com.simprints.infra.sync.usecase.internal

import com.simprints.infra.eventsync.status.models.EventSyncState
import com.simprints.infra.eventsync.sync.EventSyncStateProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSyncUseCase @Inject constructor(
    private val eventSyncStateProcessor: EventSyncStateProcessor,
) {

    internal operator fun invoke(): Flow<EventSyncState> =
        eventSyncStateProcessor.getLastSyncState().distinctUntilChanged()
}
