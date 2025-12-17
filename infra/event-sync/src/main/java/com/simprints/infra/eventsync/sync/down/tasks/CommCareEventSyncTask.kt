package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.sync.common.EnrolmentRecordFactory
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.COMMCARE_SYNC
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal class CommCareEventSyncTask @Inject constructor(
    enrolmentRecordRepository: EnrolmentRecordRepository,
    eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    enrolmentRecordFactory: EnrolmentRecordFactory,
    configManager: ConfigManager,
    timeHelper: TimeHelper,
    eventRepository: EventRepository,
    private val commCareEventDataSource: CommCareEventDataSource,
) : BaseEventDownSyncTask(
        enrolmentRecordRepository,
        eventDownSyncScopeRepository,
        enrolmentRecordFactory,
        configManager,
        timeHelper,
        eventRepository,
    ) {
    override suspend fun fetchEvents(
        operation: EventDownSyncOperation,
        scope: CoroutineScope,
        requestId: String,
    ): EventFetchResult {
        Simber.i("CommCareEventSyncTask started", tag = COMMCARE_SYNC)
        val result = commCareEventDataSource.getEvents(operation.queryEvent)

        return EventFetchResult(
            eventFlow = result.eventFlow,
            totalCount = result.totalCount,
        )
    }

    override fun shouldRethrowError(throwable: Throwable): Boolean {
        // Return true to re-throw specific exceptions that should not be handled by the base class
        return throwable is SecurityException || throwable is IllegalStateException
    }

    // Override to track subject IDs present in CommCare and update CommCareSyncCache
    override suspend fun onEventsProcessed(events: List<EnrolmentRecordEvent>) = commCareEventDataSource.onEventsProcessed(events)
}
