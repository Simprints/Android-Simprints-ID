package com.simprints.infra.eventsync.sync.down.tasks

import androidx.annotation.VisibleForTesting
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.store.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.FAILED
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.RUNNING
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncResult
import com.simprints.infra.eventsync.sync.common.SYNC_LOG_TAG
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

internal class EventDownSyncTask @Inject constructor(
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
    private val subjectFactory: SubjectFactory,
    private val configManager: ConfigManager,
    private val timeHelper: TimeHelper,
    private val eventRemoteDataSource: EventRemoteDataSource,
    private val eventRepository: EventRepository,
) {

    suspend fun downSync(
        scope: CoroutineScope,
        operation: EventDownSyncOperation,
        eventScope: EventScope,
    ): Flow<EventDownSyncProgress> = flow {
        var lastOperation = operation.copy()
        var count = 0
        val batchOfEventsToProcess = mutableListOf<EnrolmentRecordEvent>()
        val requestStartTime = timeHelper.now()

        var firstEventTimestamp: Timestamp? = null
        val requestId = UUID.randomUUID().toString()
        var result: EventDownSyncResult? = null
        var errorType: String? = null

        try {
            result = eventRemoteDataSource.getEvents(
                requestId,
                operation.queryEvent.fromDomainToApi(),
                scope
            )

            result.eventStream
                .consumeAsFlow()
                .catch {
                    // Track a case when event stream is closed due to a parser error,
                    // but the exception is handled gracefully and channel is closed correctly.
                    errorType = it.javaClass.simpleName
                }
                .collect {
                    batchOfEventsToProcess.add(it)
                    count++
                    //We immediately process the first event to initialise a progress
                    if (batchOfEventsToProcess.size > EVENTS_BATCH_SIZE || count == 1) {
                        if (count == 1) {
                            // Track the moment when the first event is received
                            firstEventTimestamp = timeHelper.now()
                        }

                        lastOperation =
                            processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
                        emitProgress(lastOperation, count, result.totalCount)
                        batchOfEventsToProcess.clear()
                    }
                }

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
            emitProgress(lastOperation, count, result.totalCount)

            lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now().ms)
            emitProgress(lastOperation, count, result.totalCount)
        } catch (t: Throwable) {
            if (t is RemoteDbNotSignedInException) {
                throw t
            }

            Simber.d(t)
            errorType = t.javaClass.simpleName

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
            emitProgress(lastOperation, count, count)

            lastOperation = lastOperation.copy(state = FAILED, lastSyncTime = timeHelper.now().ms)
            emitProgress(lastOperation, count, count)
        }

        if (count > 0 || errorType != null) {
            // Track only events that have any useful data
            eventRepository.addOrUpdateEvent(
                eventScope,
                EventDownSyncRequestEvent(
                    createdAt = requestStartTime,
                    endedAt = timeHelper.now(),
                    requestId = requestId,
                    query = operation.queryEvent.let { query ->
                        EventDownSyncRequestEvent.QueryParameters(
                            query.moduleId,
                            query.attendantId,
                            query.subjectId,
                            query.modes.map { it.name },
                            query.lastEventId
                        )
                    },
                    msToFirstResponseByte = firstEventTimestamp?.let { it.ms - requestStartTime.ms },
                    eventRead = count,
                    errorType = errorType,
                    responseStatus = result?.status,
                )
            )
        }
    }

    private suspend fun FlowCollector<EventDownSyncProgress>.emitProgress(
        lastOperation: EventDownSyncOperation,
        count: Int,
        max: Int?,
    ) {
        eventDownSyncScopeRepository.insertOrUpdate(lastOperation)
        this.emit(EventDownSyncProgress(lastOperation, count, max))
    }

    private suspend fun processBatchedEvents(
        operation: EventDownSyncOperation,
        batchOfEventsToProcess: MutableList<EnrolmentRecordEvent>,
        lastOperation: EventDownSyncOperation,
    ): EventDownSyncOperation {

        val actions = batchOfEventsToProcess.map { event ->
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
            }
        }.flatten()

        enrolmentRecordRepository.performActions(actions)

        Simber.tag(SYNC_LOG_TAG).d("[DOWN_SYNC_HELPER] batch processed")

        return if (batchOfEventsToProcess.size > 0) {
            lastOperation.copy(
                state = RUNNING,
                lastEventId = batchOfEventsToProcess.last().id,
                lastSyncTime = timeHelper.now().ms,
            )
        } else {
            lastOperation.copy(state = RUNNING, lastSyncTime = timeHelper.now().ms)
        }
    }

    @VisibleForTesting
    fun handleSubjectCreationEvent(event: EnrolmentRecordCreationEvent): List<SubjectAction> {
        val subject = subjectFactory.buildSubjectFromCreationPayload(event.payload)
        return if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
            listOf(Creation(subject))
        } else {
            emptyList()
        }
    }

    private suspend fun handleSubjectMoveEvent(
        operation: EventDownSyncOperation,
        event: EnrolmentRecordMoveEvent,
    ): List<SubjectAction> {
        val modulesIdsUnderSyncing = operation.queryEvent.moduleId
        val attendantUnderSyncing = operation.queryEvent.attendantId
        val enrolmentRecordDeletion = event.payload.enrolmentRecordDeletion
        val enrolmentRecordCreation = event.payload.enrolmentRecordCreation

        val actions = mutableListOf<SubjectAction>()
        when {
            !modulesIdsUnderSyncing.isNullOrEmpty() -> {

                /**
                 * handleSubjectMoveEvent is executed by each worker to process a new moveEvent.
                 * The deletion part of a move is executed if:
                 * 1) the deletion is part of the EventDownSyncOperation (deletion.moduleId == op.moduleId)
                 * AND
                 * 2) the creation is not synced by other workers.
                 * Required to avoid a race condition:
                 * Let's assume that SID is syncing by module1 (by worker1) and module2 (by worker2) and a subjectA
                 * is moved from module1 to module2.
                 * Both workers will receive a move event (worker1 to delete subjectA and worker2 to create subjectA), but
                 * because workers run in parallel, the worker1 may execute the deletion after worker2 has done the
                 * creation. So finally the subject is not moved, but actually deleted.
                 * So if another worker is going to create (insertOrUpdate) the subject for a different module,
                 * then the deletion will be ignored and a the update is executed.)
                 */
                if (enrolmentRecordDeletion.isUnderSyncingByCurrentDownSyncOperation(operation) &&
                    (!enrolmentRecordCreation.isUnderOverallSyncing())
                ) {

                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (enrolmentRecordCreation.isUnderSyncingByCurrentDownSyncOperation(operation)) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                        actions.add(
                            it
                        )
                    }
                }
            }

            attendantUnderSyncing != null -> {
                if (attendantUnderSyncing == enrolmentRecordDeletion.attendantId.value) {
                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (attendantUnderSyncing == enrolmentRecordCreation.attendantId.value) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                        actions.add(
                            it
                        )
                    }
                }
            }

            else -> {
                actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                    actions.add(
                        it
                    )
                }
            }
        }

        return actions
    }

    private fun createASubjectActionFromRecordCreation(enrolmentRecordCreation: EnrolmentRecordCreationInMove?): Creation? =
        enrolmentRecordCreation?.let {
            val subject = subjectFactory.buildSubjectFromMovePayload(it)
            if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
                Creation(subject)
            } else {
                null
            }
        }


    private fun EnrolmentRecordDeletionInMove.isUnderSyncingByCurrentDownSyncOperation(op: EventDownSyncOperation) =
        op.queryEvent.moduleId == moduleId.value

    private fun EnrolmentRecordCreationInMove.isUnderSyncingByCurrentDownSyncOperation(op: EventDownSyncOperation) =
        op.queryEvent.moduleId == moduleId.value

    private suspend fun EnrolmentRecordCreationInMove.isUnderOverallSyncing() =
        moduleId.value.partOf(configManager.getDeviceConfiguration().selectedModules.values())

    private fun String.partOf(modules: List<String>) = modules.contains(this)

    private fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent): List<SubjectAction> =
        listOf(Deletion(event.payload.subjectId))

    companion object {

        const val EVENTS_BATCH_SIZE = 200
    }
}
