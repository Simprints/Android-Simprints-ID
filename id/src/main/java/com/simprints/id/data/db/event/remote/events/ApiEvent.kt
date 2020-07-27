package com.simprints.id.data.db.event.remote.events

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationEvent.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMoveEvent.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.subject.fromApiToDomain

@Keep
class ApiEvents(val events: List<ApiEvent>)

@Keep
open class ApiEvent(val id: String,
                    val labels: ApiEventLabels,
                    val payload: ApiEventPayload)

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())

fun ApiEvent.fromApiToDomain() =
    when (payload.type) {
        ENROLMENT_RECORD_CREATION -> EnrolmentRecordCreationEvent(id, labels.fromApiToDomain(), (payload as ApiEnrolmentRecordCreationPayload).fromApiToDomain(), payload.type.fromApiToDomain())
        ENROLMENT_RECORD_DELETION -> EnrolmentRecordDeletionEvent(id, labels.fromApiToDomain(), (payload as ApiEnrolmentRecordDeletionPayload).fromApiToDomain(), payload.type.fromApiToDomain())
        ENROLMENT_RECORD_MOVE -> EnrolmentRecordMoveEvent(id, labels.fromApiToDomain(), (payload as ApiEnrolmentRecordMovePayload).fromApiToDomain(), payload.type.fromApiToDomain())
        CALLOUT, CALLBACK, ARTIFICIAL_TERMINATION,
        AUTHENTICATION, CONSENT, ENROLMENT, AUTHORIZATION,
        FINGERPRINT_CAPTURE, ONE_TO_ONE_MATCH, ONE_TO_MANY_MATCH,
        PERSON_CREATION, ALERT_SCREEN, GUID_SELECTION, CONNECTIVITY_SNAPSHOT,
        REFUSAL, CANDIDATE_READ, SCANNER_CONNECTION, VERO_2_INFO_SNAPSHOT,
        SCANNER_FIRMWARE_UPDATE, INVALID_INTENT, SUSPICIOUS_INTENT,
        INTENT_PARSING, COMPLETION_CHECK, SESSION_CAPTURE,
        FACE_ONBOARDING_COMPLETE, FACE_FALLBACK_CAPTURE, FACE_CAPTURE,
        FACE_CAPTURE_CONFIRMATION, FACE_CAPTURE_RETRY -> throw UnsupportedOperationException("Impossible to convert ${payload.type} fromApiToDomain")
    }
