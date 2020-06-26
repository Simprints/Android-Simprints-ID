package com.simprints.id.data.db.event.domain.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.Event
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationEvent.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import java.util.*

@Keep
class EnrolmentRecordMoveEvent(
    enrolmentRecordCreation: EnrolmentRecordCreationPayload?,
    enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
) : Event(
    UUID.randomUUID().toString(),
    listOf(), //StopShip: to check with cloud - labels empty?
    EnrolmentRecordMovePayload(enrolmentRecordCreation, enrolmentRecordDeletion)) {

    data class EnrolmentRecordMovePayload(
        val enrolmentRecordCreation: EnrolmentRecordCreationPayload?,
        val enrolmentRecordDeletion: EnrolmentRecordDeletionPayload
    ) : EventPayload(EventPayloadType.ENROLMENT_RECORD_MOVE, 0)
}
