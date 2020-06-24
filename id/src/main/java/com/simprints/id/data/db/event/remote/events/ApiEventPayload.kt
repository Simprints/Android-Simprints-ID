package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.events.EventPayload
import com.simprints.id.data.db.event.domain.events.EventPayloadType
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMovePayload

@Keep
abstract class ApiEventPayload(@Transient val type: ApiEventPayloadType)

fun EventPayload.fromDomainToApi() = when(this.type) {
    EventPayloadType.ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationPayload(this as EnrolmentRecordCreationPayload)
    EventPayloadType.ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionPayload(this as EnrolmentRecordDeletionPayload)
    EventPayloadType.ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMovePayload(this as EnrolmentRecordMovePayload)
    EventPayloadType.ARTIFICIAL_TERMINATION -> TODO()
    EventPayloadType.AUTHENTICATION -> TODO()
    EventPayloadType.CONSENT -> TODO()
    EventPayloadType.ENROLMENT -> TODO()
    EventPayloadType.AUTHORIZATION -> TODO()
    EventPayloadType.FINGERPRINT_CAPTURE -> TODO()
    EventPayloadType.ONE_TO_ONE_MATCH -> TODO()
    EventPayloadType.ONE_TO_MANY_MATCH -> TODO()
    EventPayloadType.PERSON_CREATION -> TODO()
    EventPayloadType.ALERT_SCREEN -> TODO()
    EventPayloadType.GUID_SELECTION -> TODO()
    EventPayloadType.CONNECTIVITY_SNAPSHOT -> TODO()
    EventPayloadType.REFUSAL -> TODO()
    EventPayloadType.CANDIDATE_READ -> TODO()
    EventPayloadType.SCANNER_CONNECTION -> TODO()
    EventPayloadType.VERO_2_INFO_SNAPSHOT -> TODO()
    EventPayloadType.SCANNER_FIRMWARE_UPDATE -> TODO()
    EventPayloadType.INVALID_INTENT -> TODO()
    EventPayloadType.CALLOUT_CONFIRMATION -> TODO()
    EventPayloadType.CALLOUT_IDENTIFICATION -> TODO()
    EventPayloadType.CALLOUT_ENROLMENT -> TODO()
    EventPayloadType.CALLOUT_VERIFICATION -> TODO()
    EventPayloadType.CALLOUT_LAST_BIOMETRICS -> TODO()
    EventPayloadType.CALLBACK_IDENTIFICATION -> TODO()
    EventPayloadType.CALLBACK_ENROLMENT -> TODO()
    EventPayloadType.CALLBACK_REFUSAL -> TODO()
    EventPayloadType.CALLBACK_VERIFICATION -> TODO()
    EventPayloadType.CALLBACK_ERROR -> TODO()
    EventPayloadType.SUSPICIOUS_INTENT -> TODO()
    EventPayloadType.INTENT_PARSING -> TODO()
    EventPayloadType.COMPLETION_CHECK -> TODO()
    EventPayloadType.CALLBACK_CONFIRMATION -> TODO()
}
