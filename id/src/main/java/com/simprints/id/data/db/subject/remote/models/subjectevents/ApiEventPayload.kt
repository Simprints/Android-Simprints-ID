package com.simprints.id.data.db.subject.remote.models.subjectevents

import androidx.annotation.Keep
import com.simprints.id.data.db.subject.domain.subjectevents.*

@Keep
abstract class ApiEventPayload(@Transient val type: ApiEventPayloadType)

fun EventPayload.fromDomainToApi() = when(this.type) {
    EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationPayload(this as EnrolmentRecordCreationPayload)
    EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionPayload(this as EnrolmentRecordDeletionPayload)
    EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMovePayload(this as EnrolmentRecordMovePayload)
}
