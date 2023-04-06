package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.infra.events.event.domain.EventCount
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncScope
import javax.inject.Inject

internal class EventDownSyncCountTask @Inject constructor(
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
) {

    suspend fun getCount(downSyncScope: EventDownSyncScope): List<EventCount> = downSyncScope
        .operations
        .map { eventDownSyncScopeRepository.refreshState(it) }
        .map { eventRemoteDataSource.count(it.queryEvent.fromDomainToApi()) }
        .flatten()
}
