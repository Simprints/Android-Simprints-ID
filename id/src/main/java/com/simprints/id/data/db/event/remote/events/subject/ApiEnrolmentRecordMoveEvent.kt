package com.simprints.id.data.db.event.remote.events.subject

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent.ApiEnrolmentRecordDeletionPayload

@io.realm.internal.Keep
class ApiEnrolmentRecordMoveEvent(domainEvent: EnrolmentRecordMoveEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.map { it.fromDomainToApi() }.toMap(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiEnrolmentRecordMovePayload(
        createdAt: Long,
        version: Int,
        val enrolmentRecordCreation: ApiEnrolmentRecordCreationPayload?,
        val enrolmentRecordDeletion: ApiEnrolmentRecordDeletionPayload
    ) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_MOVE, version, createdAt) {

        constructor(payload: EnrolmentRecordMovePayload) : this(
            payload.createdAt,
            payload.eventVersion,
            payload.enrolmentRecordCreation?.fromDomainToApi() as ApiEnrolmentRecordCreationPayload?,
            payload.enrolmentRecordDeletion.fromDomainToApi() as ApiEnrolmentRecordDeletionPayload)
    }
}
