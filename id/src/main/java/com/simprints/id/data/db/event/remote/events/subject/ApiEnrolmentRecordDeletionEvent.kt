package com.simprints.id.data.db.event.remote.events.subject

import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.ApiEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType
import com.simprints.id.data.db.event.remote.events.fromDomainToApi
import io.realm.internal.Keep

@Keep
class ApiEnrolmentRecordDeletionEvent(domainEvent: EnrolmentRecordDeletionEvent) :
    ApiEvent(
        domainEvent.id,
        domainEvent.labels.fromDomainToApi(),
        domainEvent.payload.fromDomainToApi()) {

    @Keep
    class ApiEnrolmentRecordDeletionPayload(
        createdAt: Long,
        version: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String
    ) : ApiEventPayload(ApiEventPayloadType.ENROLMENT_RECORD_DELETION, version, createdAt) {

        constructor(payload: EnrolmentRecordDeletionPayload) :
            this(payload.createdAt, payload.eventVersion, payload.subjectId, payload.projectId, payload.moduleId, payload.attendantId)
    }
}
