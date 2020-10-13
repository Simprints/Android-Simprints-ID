package com.simprints.id.services.sync.events.down

import androidx.annotation.VisibleForTesting
import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.SubjectAction
import com.simprints.id.data.db.subject.domain.SubjectAction.Creation
import com.simprints.id.data.db.subject.domain.SubjectAction.Deletion
import com.simprints.id.data.db.subject.domain.SubjectFactory
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
                    Timber.d("[DOWN_SYNC_HELPER] Event Received")

                    batchOfEventsToProcess.add(it)
                    count++
                    //We immediately process the first event to initialise a progress
                    if (batchOfEventsToProcess.size > EVENTS_BATCH_SIZE || count == 1) {
                        lastOperation = processBatchedEvents(batchOfEventsToProcess, lastOperation)
                        emitProgress(lastOperation, count)
                        batchOfEventsToProcess.clear()
                    }
                }

                lastOperation = processBatchedEvents(batchOfEventsToProcess, lastOperation)
                emitProgress(lastOperation, count)

                lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)

                close()

            } catch (t: Throwable) {
                Timber.d(t)

                lastOperation = processBatchedEvents(batchOfEventsToProcess, lastOperation)
                emitProgress(lastOperation, count)

                lastOperation = lastOperation.copy(state = FAILED, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)
                close()
            }

        }

    private suspend fun processBatchedEvents(batchOfEventsToProcess: MutableList<Event>,
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
                    handleSubjectMoveEvent(event as EnrolmentRecordMoveEvent)
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

    @VisibleForTesting
    fun handleSubjectMoveEvent(event: EnrolmentRecordMoveEvent): List<SubjectAction> =
        mutableListOf<SubjectAction>().apply {
            event.payload.enrolmentRecordCreation?.let {
                val subject = subjectFactory.buildSubjectFromMovePayload(it)
                if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
                    add(Creation(subject))
                }
            }

            add(Deletion(event.payload.enrolmentRecordDeletion.subjectId))
        }

    @VisibleForTesting
    fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent): List<SubjectAction> =
        listOf(Deletion(event.payload.subjectId))

    companion object {
        const val EVENTS_BATCH_SIZE = 200
    }
}
