package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EnrolmentRecordFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

internal class SimprintsEventDownSyncTask @Inject constructor(
    enrolmentRecordRepository: EnrolmentRecordRepository,
    eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    enrolmentRecordFactory: EnrolmentRecordFactory,
    configRepository: ConfigRepository,
    timeHelper: TimeHelper,
    eventRepository: EventRepository,
    private val eventRemoteDataSource: EventRemoteDataSource,
) : BaseEventDownSyncTask(
        enrolmentRecordRepository,
        eventDownSyncScopeRepository,
        enrolmentRecordFactory,
        configRepository,
        timeHelper,
        eventRepository,
    ) {
    override suspend fun fetchEvents(
        operation: EventDownSyncOperation,
        scope: CoroutineScope,
        requestId: String,
    ): EventFetchResult {
        val result = eventRemoteDataSource.getEvents(
            requestId,
            operation.queryEvent.fromDomainToApi(),
            scope,
        )

        val eventFlow = result.eventStream
            .consumeAsFlow()
            .catch {
                // Track a case when event stream is closed due to a parser error,
                // but the exception is handled gracefully and channel is closed correctly.
                // The error will be handled by the base class
                throw it
            }

        return EventFetchResult(
            eventFlow = eventFlow,
            totalCount = result.totalCount,
            status = result.status,
        )
    }

    override fun shouldRethrowError(throwable: Throwable): Boolean {
        // Return true to re-throw specific exceptions that should not be handled by the base class
        return throwable is RemoteDbNotSignedInException
    }
}
