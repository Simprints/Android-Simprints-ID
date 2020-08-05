package com.simprints.id.services.sync.events.down

import com.simprints.id.data.db.event.EventRepository
import com.simprints.id.data.db.event.domain.models.EventType.*
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepo
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation
import com.simprints.id.data.db.events_sync.down.domain.EventDownSyncOperation.Result.DownSyncState.RUNNING
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.tools.TimeHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce

class EventDownSyncHelperImpl(val subjectRepository: SubjectRepository,
                              val eventRepository: EventRepository,
                              private val eventDownSyncScopeRepo: EventDownSyncScopeRepo,
                              val timeHelper: TimeHelper) : EventDownSyncHelper {

    override suspend fun downSync(scope: CoroutineScope,
                                  operation: EventDownSyncOperation): ReceiveChannel<EventDownSyncProgress> =
        scope.produce {
            var count = 0
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

                    val newOperation = operation.copy(lastResult = EventDownSyncOperation.Result(RUNNING, event.id, timeHelper.now()))
                    eventDownSyncScopeRepo.insertOrUpdate(newOperation)

                    if (!this.isClosedForSend) {
                        this.send(EventDownSyncProgress(newOperation, count))
                    }
                }
            }
        }


    private fun handleSubjectDeletionEvent(event: EnrolmentRecordDeletionEvent) {

    }

    private fun handleSubjectMoveEvent(event: EnrolmentRecordMoveEvent) {

    }

    private fun handleSubjectCreationEvent(event: EnrolmentRecordCreationEvent) {

    }
}
