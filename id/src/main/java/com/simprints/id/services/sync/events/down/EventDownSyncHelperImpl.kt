package com.simprints.id.services.sync.events.down

import androidx.annotation.VisibleForTesting
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordCreationInMove
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordDeletionInMove
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.domain.SubjectAction.Deletion
import com.simprints.id.data.db.subject.domain.SubjectFactory
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.sync.events.common.SYNC_LOG_TAG
import com.simprints.id.tools.time.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class EventDownSyncHelperImpl(val subjectRepository: SubjectRepository,
                              val eventRepository: EventRepository,
                              private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
                              private val subjectFactory: SubjectFactory,
                              private val preferencesManager: PreferencesManager,
                              val timeHelper: TimeHelper) : EventDownSyncHelper {

    override suspend fun countForDownSync(operation: EventDownSyncOperation): List<EventCount> =
        eventRepository.countEventsToDownload(operation.queryEvent)

    override suspend fun downSync(scope: CoroutineScope,
                                  operation: EventDownSyncOperation): ReceiveChannel<EventDownSyncProgress> =

        scope.produce(capacity = Channel.UNLIMITED) {
            var lastOperation = operation.copy()
            var count = 0
            val batchOfEventsToProcess = mutableListOf<Event>()

            try {
                eventRepository.downloadEvents(scope, operation.queryEvent).consumeEach {
                    batchOfEventsToProcess.add(it)
                    count++
                    //We immediately process the first event to initialise a progress
                    if (batchOfEventsToProcess.size > EVENTS_BATCH_SIZE || count == 1) {
                        lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
                        emitProgress(lastOperation, count)
                        batchOfEventsToProcess.clear()
                    }
                }

                lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
                emitProgress(lastOperation, count)

                lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)

                close()

            } catch (t: Throwable) {
                Timber.d(t)

                lastOperation = processBatchedEvents(operation, batchOfEventsToProcess, lastOperation)
                emitProgress(lastOperation, count)

                lastOperation = lastOperation.copy(state = FAILED, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)
                close()
            }

        }

    private suspend fun processBatchedEvents(operation: EventDownSyncOperation,
                                             batchOfEventsToProcess: MutableList<Event>,
                                             lastOperation: EventDownSyncOperation): EventDownSyncOperation {

        val actions = batchOfEventsToProcess.map { event ->
            return@map when (event.type) {
                ENROLMENT_RECORD_CREATION -> {
                    handleSubjectCreationEvent(event as EnrolmentRecordCreationEvent)
                }
                ENROLMENT_RECORD_DELETION -> {
                    handleSubjectDeletionEvent(event as EnrolmentRecordDeletionEvent)
                }
                ENROLMENT_RECORD_MOVE -> {
                    handleSubjectMoveEvent(operation, event as EnrolmentRecordMoveEvent)
                }
                else -> {
                    emptyList()
                }
            }
        }.flatten()

        subjectRepository.performActions(actions)

        Timber.tag(SYNC_LOG_TAG).d("[DOWN_SYNC_HELPER] batch processed")

        return if (batchOfEventsToProcess.size > 0) {
            lastOperation.copy(state = RUNNING, lastEventId = batchOfEventsToProcess.last().id, lastSyncTime = timeHelper.now())
        } else {
            lastOperation.copy(state = RUNNING, lastSyncTime = timeHelper.now())
        }
    }


    private suspend fun ProducerScope<EventDownSyncProgress>.emitProgress(lastOperation: EventDownSyncOperation, count: Int) {
        Timber.d("[DOWN_SYNC_HELPER] Emit progress")

        if (!this.isClosedForSend) {
            withContext(Dispatchers.IO) {
                eventDownSyncScopeRepository.insertOrUpdate(lastOperation)
            }
            this.send(EventDownSyncProgress(lastOperation, count))
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

    fun handleSubjectMoveEvent(operation: EventDownSyncOperation,
                               event: EnrolmentRecordMoveEvent): List<SubjectAction> {
        val modulesIdsUnderSyncing = operation.queryEvent.moduleIds
        val attendantUnderSyncing = operation.queryEvent.attendantId
        val enrolmentRecordDeletion = event.payload.enrolmentRecordDeletion
        val enrolmentRecordCreation = event.payload.enrolmentRecordCreation

        val actions = mutableListOf<SubjectAction>()
        when {
            modulesIdsUnderSyncing != null && modulesIdsUnderSyncing.isNotEmpty() -> {

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
                    (!enrolmentRecordCreation.isUnderOverallSyncing())) {

                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (enrolmentRecordCreation.isUnderSyncingByCurrentDownSyncOperation(operation)) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let { actions.add(it) }
                }
            }
            attendantUnderSyncing != null -> {
                if (attendantUnderSyncing == enrolmentRecordDeletion.attendantId) {
                    actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                }

                if (attendantUnderSyncing == enrolmentRecordCreation.attendantId) {
                    createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let { actions.add(it) }
                }
            }
            else -> {
                actions.add(Deletion(enrolmentRecordDeletion.subjectId))
                createASubjectActionFromRecordCreation(enrolmentRecordCreation)?.let { actions.add(it) }
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
        op.queryEvent.moduleIds?.let { moduleId.partOf(it) } ?: false

    private fun EnrolmentRecordCreationInMove.isUnderSyncingByCurrentDownSyncOperation(op: EventDownSyncOperation) =
        op.queryEvent.moduleIds?.let { moduleId.partOf(it) } ?: false

    private fun EnrolmentRecordCreationInMove.isUnderOverallSyncing() =
        moduleId.partOf(preferencesManager.selectedModules.toList())

    private fun String.partOf(modules: List<String>) = modules.contains(this)

    fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent): List<SubjectAction> =
        listOf(Deletion(event.payload.subjectId))

    companion object {
        const val EVENTS_BATCH_SIZE = 200
    }
}
