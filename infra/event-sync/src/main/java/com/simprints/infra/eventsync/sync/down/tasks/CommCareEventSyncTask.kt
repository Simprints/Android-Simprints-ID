package com.simprints.infra.eventsync.sync.down.tasks

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.RUNNING
import com.simprints.infra.eventsync.sync.common.SubjectFactory
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

internal class CommCareEventSyncTask @Inject constructor(
    enrolmentRecordRepository: EnrolmentRecordRepository,
    eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    subjectFactory: SubjectFactory,
    configManager: ConfigManager,
    timeHelper: TimeHelper,
    eventRepository: EventRepository,
    private val commCareEventDataSource: CommCareEventDataSource,
) : BaseEventDownSyncTask(
    enrolmentRecordRepository,
    eventDownSyncScopeRepository,
    subjectFactory,
    configManager,
    timeHelper,
    eventRepository,
) {
    private var subjectIdsPresentInCommCare = mutableSetOf<String>()

    override suspend fun fetchEvents(
        operation: EventDownSyncOperation,
        scope: CoroutineScope,
        requestId: String,
    ): EventFetchResult {
        Simber.d("CommCareEventSyncTask started", tag = "CommCareSync")
        val result = commCareEventDataSource.getEvents()

        return EventFetchResult(
            eventFlow = result.eventFlow,
            totalCount = result.totalCount,
        )
    }

    override fun handleSyncError(throwable: Throwable): Boolean {
        // Return true to re-throw specific exceptions that should not be handled by the base class
        return throwable is SecurityException || throwable is IllegalStateException
    }

    override suspend fun performPostSyncCleanup(project: Project, count: Int) {
        //Don't trigger if count is 0 because it might be due to CommCare logout
        if (count > 0) {
            deleteSubjectsNotInCommCare(project)
        }

        Simber.d("CommCareEventSyncTask finished", tag = "CommCareSync")
    }

    // Override to track subject IDs present in CommCare
    //TODO(milen): add a callback for subjectIds to avoid overriding
    override suspend fun processBatchedEvents(
        operation: EventDownSyncOperation,
        batchOfEventsToProcess: MutableList<EnrolmentRecordEvent>,
        lastOperation: EventDownSyncOperation,
        project: Project,
    ): EventDownSyncOperation {
        val actions = batchOfEventsToProcess
            .map { event ->
                return@map when (event.type) {
                    EnrolmentRecordEventType.EnrolmentRecordCreation -> {
                        handleSubjectCreationEvent(event as EnrolmentRecordCreationEvent)
                    }

                    EnrolmentRecordEventType.EnrolmentRecordDeletion -> {
                        handleSubjectDeletionEvent(event as EnrolmentRecordDeletionEvent)
                    }

                    EnrolmentRecordEventType.EnrolmentRecordMove -> {
                        handleSubjectMoveEvent(operation, event as EnrolmentRecordMoveEvent)
                    }

                    EnrolmentRecordEventType.EnrolmentRecordUpdate -> {
                        handleSubjectUpdateEvent(event as EnrolmentRecordUpdateEvent)
                    }
                }
            }.flatten()

        enrolmentRecordRepository.performActions(actions, project)
        actions.forEach { action ->
            if (action is Creation) {
                subjectIdsPresentInCommCare.add(action.subject.subjectId)
            }
        }

        return if (batchOfEventsToProcess.isNotEmpty()) {
            lastOperation.copy(
                state = RUNNING,
                lastEventId = batchOfEventsToProcess.last().id,
                lastSyncTime = timeHelper.now().ms,
            )
        } else {
            lastOperation.copy(state = RUNNING, lastSyncTime = timeHelper.now().ms)
        }
    }

    private suspend fun deleteSubjectsNotInCommCare(project: Project) {
        val deleteActions = enrolmentRecordRepository.getAllSubjectIds()
            .filterNot { subjectIdsPresentInCommCare.contains(it) }
            .map { Deletion(it) }
        if (deleteActions.isNotEmpty()) {
            enrolmentRecordRepository.performActions(deleteActions, project)
            Simber.i("Deleted ${deleteActions.size} subjects not present in CommCare", tag = "CommCareSync")
        }
    }
}
