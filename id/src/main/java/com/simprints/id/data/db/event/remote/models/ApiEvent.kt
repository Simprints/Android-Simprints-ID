package com.simprints.id.data.db.event.remote.models

import androidx.annotation.Keep
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.models.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.models.subject.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.models.subject.fromApiToDomain

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
        ENROLMENT_RECORD_CREATION -> (payload as ApiEnrolmentRecordCreationPayload).fromApiToDomain().let { EnrolmentRecordCreationEvent(id, labels.fromApiToDomain(), it, it.type) }
        ENROLMENT_RECORD_DELETION -> (payload as ApiEnrolmentRecordDeletionPayload).fromApiToDomain().let { EnrolmentRecordDeletionEvent(id, labels.fromApiToDomain(), it, it.type) }
        ENROLMENT_RECORD_MOVE -> (payload as ApiEnrolmentRecordMovePayload).fromApiToDomain().let { EnrolmentRecordMoveEvent(id, labels.fromApiToDomain(), it, it.type) }
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
