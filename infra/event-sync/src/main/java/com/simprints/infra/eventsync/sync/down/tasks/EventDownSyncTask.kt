package com.simprints.infra.eventsync.sync.down.tasks

import androidx.annotation.VisibleForTesting
import com.simprints.core.domain.tokenization.values
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.authstore.exceptions.RemoteDbNotSignedInException
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Creation
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectAction.Deletion
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.events.event.domain.models.scope.EventScope
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEventType
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordUpdateEvent
import com.simprints.infra.eventsync.event.remote.EventRemoteDataSource
import com.simprints.infra.eventsync.status.down.EventDownSyncScopeRepository
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.COMPLETE
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.FAILED
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncOperation.DownSyncState.RUNNING
import com.simprints.infra.eventsync.status.down.domain.EventDownSyncResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SYNC
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
    fun downSync(
        scope: CoroutineScope,
        operation: EventDownSyncOperation,
        eventScope: EventScope,
        project: Project,
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
                scope,
            )

            result.eventStream
                .consumeAsFlow()
                .catch {
                    // Track a case when event stream is closed due to a parser error,
                    // but the exception is handled gracefully and channel is closed correctly.
                    errorType = it.javaClass.simpleName
                }.collect {
                    batchOfEventsToProcess.add(it)
                    count++
                    // We immediately process the first event to initialise a progress
                    if (batchOfEventsToProcess.size > EVENTS_BATCH_SIZE || count == 1) {
                        if (count == 1) {
                            // Track the moment when the first event is received
                            firstEventTimestamp = timeHelper.now()
                        }

                        lastOperation =
                            processBatchedEvents(operation, batchOfEventsToProcess, lastOperation, project)
                        emitProgress(lastOperation, count, result.totalCount)
                        batchOfEventsToProcess.clear()
                    }
                }

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation, project)
            emitProgress(lastOperation, count, result.totalCount)

            lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now().ms)
            emitProgress(lastOperation, count, result.totalCount)
        } catch (t: Throwable) {
            if (t is RemoteDbNotSignedInException) {
                throw t
            }

            Simber.i("Down sync error", t, tag = SYNC)
            errorType = t.javaClass.simpleName

            lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation, project)
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
                            query.lastEventId,
                        )
                    },
                    msToFirstResponseByte = firstEventTimestamp?.let { it.ms - requestStartTime.ms },
                    eventRead = count,
                    errorType = errorType,
                    responseStatus = result?.status,
                ),
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
        val enrolmentRecordDeletion = event.payload.enrolmentRecordDeletion
        val enrolmentRecordCreation = event.payload.enrolmentRecordCreation

        val actions = mutableListOf<SubjectAction>()

        actions.add(Deletion(enrolmentRecordDeletion.subjectId))
        if (shouldBeSynced(enrolmentRecordCreation, operation)) {
            createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let {
                actions.add(it)
            }
        }

        return actions
    }

    private suspend fun shouldBeSynced(
        enrolmentRecordCreation: EnrolmentRecordCreationInMove,
        operation: EventDownSyncOperation,
    ): Boolean = when {
        // When syncing by module, check whether record was moved in a module selected for syncing
        operation.isSyncingByModule() -> {
            configManager
                .getDeviceConfiguration()
                .selectedModules
                .values()
                .contains(enrolmentRecordCreation.moduleId.value)
        }

        // When syncing by attendant, check whether record was moved to the attendant we are  syncing by
        operation.isSyncingByAttendant() -> {
            enrolmentRecordCreation.attendantId.value == operation.queryEvent.attendantId
        }

        // When syncing by project - record should always be synced
        else -> {
            true
        }
    }

    private fun EventDownSyncOperation.isSyncingByModule(): Boolean = !queryEvent.moduleId.isNullOrEmpty()

    private fun EventDownSyncOperation.isSyncingByAttendant(): Boolean = !queryEvent.attendantId.isNullOrEmpty()

    private fun createASubjectActionFromRecordCreation(enrolmentRecordCreation: EnrolmentRecordCreationInMove?): Creation? =
        enrolmentRecordCreation?.let {
            val subject = subjectFactory.buildSubjectFromMovePayload(it)
            if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
                Creation(subject)
            } else {
                null
            }
        }

    private fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent): List<SubjectAction> =
        listOf(Deletion(event.payload.subjectId))

    private fun handleSubjectUpdateEvent(event: EnrolmentRecordUpdateEvent): List<SubjectAction> = with(event.payload) {
        listOf(
            SubjectAction.Update(
                subjectId = subjectId,
                faceSamplesToAdd = subjectFactory.extractFaceSamplesFromBiometricReferences(biometricReferencesAdded),
                fingerprintSamplesToAdd = subjectFactory.extractFingerprintSamplesFromBiometricReferences(biometricReferencesAdded),
                referenceIdsToRemove = biometricReferencesRemoved,
            ),
        )
    }

    companion object {
        const val EVENTS_BATCH_SIZE = 200
    }
}
