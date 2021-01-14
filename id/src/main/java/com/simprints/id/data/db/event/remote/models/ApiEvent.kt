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
data class ApiEvent(val id: String,
                    val labels: ApiEventLabels,
                    val payload: ApiEventPayload)

fun Event.fromDomainToApi() =
    ApiEvent(id, labels.fromDomainToApi(), payload.fromDomainToApi())

fun ApiEvent.fromApiToDomain() =
    when (payload.type) {
        EnrolmentRecordCreation -> (payload as ApiEnrolmentRecordCreationPayload).fromApiToDomain().let { EnrolmentRecordCreationEvent(id, labels.fromApiToDomain(), it, it.type) }
        EnrolmentRecordDeletion -> (payload as ApiEnrolmentRecordDeletionPayload).fromApiToDomain().let { EnrolmentRecordDeletionEvent(id, labels.fromApiToDomain(), it, it.type) }
        EnrolmentRecordMove -> (payload as ApiEnrolmentRecordMovePayload).fromApiToDomain().let { EnrolmentRecordMoveEvent(id, labels.fromApiToDomain(), it, it.type) }
        Callout, Callback, ArtificialTermination,
        Authentication, Consent, Enrolment, Authorization,
        FingerprintCapture, OneToOneMatch, OneToManyMatch,
        PersonCreation, AlertScreen, GuidSelection, ConnectivitySnapshot,
        Refusal, CandidateRead, ScannerConnection, Vero2InfoSnapshot,
        ScannerFirmwareUpdate, InvalidIntent, SuspiciousIntent,
        IntentParsing, CompletionCheck, SessionCapture,
        FaceOnboardingComplete, FaceFallbackCapture, FaceCapture,
        FaceCaptureConfirmation, FaceCaptureRetry -> throw UnsupportedOperationException("Impossible to convert ${payload.type} fromApiToDomain. ${payload.type} is never down-synced")
    }
