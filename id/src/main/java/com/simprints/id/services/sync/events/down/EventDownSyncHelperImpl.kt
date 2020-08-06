package com.simprints.id.services.sync.events.down

import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.EventCount
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.DownSyncState.*
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource.Query
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import timber.log.Timber

class EventDownSyncHelperImpl(val subjectRepository: SubjectRepository,
                              val eventRepository: EventRepository,
                              private val eventDownSyncScopeRepository: EventDownSyncScopeRepository,
                              val timeHelper: TimeHelper) : EventDownSyncHelper {

    override suspend fun countForDownSync(operation: EventDownSyncOperation): List<EventCount> =
        eventRepository.countEventsToDownload(operation.queryEvent)

    override suspend fun downSync(scope: CoroutineScope,
                                  operation: EventDownSyncOperation): ReceiveChannel<EventDownSyncProgress> =
        scope.produce {
            var lastOperation = operation.copy()
            var count = 0

            try {
                eventRepository.downloadEvents(scope, operation.queryEvent).consumeEach {
                    it.forEach { event ->
                        count++
                        when (event.type) {
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
                            }
                        }

                        lastOperation = lastOperation.copy(state = RUNNING, lastEventId = event.id, lastSyncTime = timeHelper.now())
                        emitProgress(lastOperation, count)
                    }
                }

            } catch (t: Throwable) {
                Timber.d(t)
                lastOperation = lastOperation.copy(state = FAILED, lastSyncTime = timeHelper.now())
                emitProgress(lastOperation, count)
            }

            lastOperation = lastOperation.copy(state = COMPLETE, lastSyncTime = timeHelper.now())
            emitProgress(lastOperation, count)
        }

    private suspend fun ProducerScope<EventDownSyncProgress>.emitProgress(lastOperation: EventDownSyncOperation, count: Int) {
        if (!this.isClosedForSend) {
            eventDownSyncScopeRepository.insertOrUpdate(lastOperation)
            this.send(EventDownSyncProgress(lastOperation, count))
        }
    }

    private suspend fun handleSubjectCreationEvent(event: EnrolmentRecordCreationEvent) {
        val subject = Subject.buildSubjectFromCreationPayload(event.payload)
        if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
            subjectRepository.save(subject)
        }
    }

    private suspend fun handleSubjectMoveEvent(event: EnrolmentRecordMoveEvent) {
        event.payload.enrolmentRecordCreation?.let {
            val subject = Subject.buildSubjectFromCreationPayload(it)
            if (subject.fingerprintSamples.isNotEmpty() || subject.faceSamples.isNotEmpty()) {
                subjectRepository.save(subject)
            }
        }
        event.payload.enrolmentRecordDeletion?.let {
            subjectRepository.delete(listOf(Query(subjectId = it.subjectId)))
        }
    }

    private suspend fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent) {
        subjectRepository.delete(listOf(Query(subjectId = event.payload.subjectId)))
    }
}
