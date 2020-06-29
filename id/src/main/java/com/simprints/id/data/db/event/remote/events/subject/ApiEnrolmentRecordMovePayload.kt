package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.*
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent.ApiEnrolmentRecordDeletionPayload

@io.realm.internal.Keep
class ApiEnrolmentRecordMoveEvent(domainEvent: EnrolmentRecordMoveEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    data class ApiEnrolmentRecordMovePayload(
        val enrolmentRecordCreation: ApiEnrolmentRecordCreationPayload,
        val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionPayload
    ) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE) {

        constructor(payload: EnrolmentRecordMovePayload) :
            this(ApiEnrolmentRecordCreationPayload(payload.enrolmentRecordCreation
                ?: throw IllegalStateException("Domain creation payload should always have biometric references")),
                ApiEnrolmentRecordDeletionPayload(payload.enrolmentRecordDeletion))
    }
}
