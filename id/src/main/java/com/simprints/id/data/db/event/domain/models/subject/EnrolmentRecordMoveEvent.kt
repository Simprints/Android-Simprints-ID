package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_MOVE
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import java.util.*

@Keep
class EnrolmentRecordMoveEvent(
    createdAt: Long,
    enrolmentRecordCreation: EnrolmentRecordCreationPayload?,
    enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
) : Event(
    UUID.randomUUID().toString(),
    mutableListOf(), //STOPSHIP: to check with cloud - labels empty?
    EnrolmentRecordMovePayload(createdAt, DEFAULT_EVENT_VERSION, enrolmentRecordCreation, enrolmentRecordDeletion),
    ENROLMENT_RECORD_MOVE) {

    class EnrolmentRecordMovePayload(
        createdAt: Long,
        eventVersion: Int,
        val enrolmentRecordCreation: EnrolmentRecordCreationPayload?,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
    ) : EventPayload(ENROLMENT_RECORD_MOVE, eventVersion, createdAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
