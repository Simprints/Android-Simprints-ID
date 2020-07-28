package com.simprints.id.data.db.event.remote.models

import com.beust.klaxon.TypeAdapter
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.events.callback.ApiCallbackPayload
import com.simprints.id.data.db.event.remote.events.callout.ApiCalloutPayload
import com.simprints.id.data.db.event.remote.events.face.*
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.session.ApiSessionCapture.ApiSessionCapturePayload
import kotlin.reflect.KClass

class ApiEventPayloadAdapter : TypeAdapter<ApiEventPayload> {
    override fun classFor(type: Any): KClass<out ApiEventPayload> {
        return when (valueOf(type as String)) {
            ENROLMENT_RECORD_CREATION -> ApiEnrolmentRecordCreationPayload::class
            ENROLMENT_RECORD_DELETION -> ApiEnrolmentRecordDeletionPayload::class
            ENROLMENT_RECORD_MOVE -> ApiEnrolmentRecordMovePayload::class
            CALLOUT -> ApiCalloutPayload::class
            CALLBACK -> ApiCallbackPayload::class
            ARTIFICIAL_TERMINATION -> ApiArtificialTerminationPayload::class
            AUTHENTICATION -> ApiAuthenticationPayload::class
            CONSENT -> ApiConsentPayload::class
            ENROLMENT -> ApiEnrolmentRecordMovePayload::class
            AUTHORIZATION -> ApiAuthorizationPayload::class
            FINGERPRINT_CAPTURE -> ApiFingerprintCapturePayload::class
            ONE_TO_ONE_MATCH -> ApiOneToOneMatchPayload::class
            ONE_TO_MANY_MATCH -> ApiOneToManyMatchPayload::class
            PERSON_CREATION -> ApiPersonCreationPayload::class
            ALERT_SCREEN -> ApiAlertScreenPayload::class
            GUID_SELECTION -> ApiGuidSelectionPayload::class
            CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotPayload::class
            REFUSAL -> ApiRefusalPayload::class
            CANDIDATE_READ -> ApiCandidateReadPayload::class
            SCANNER_CONNECTION -> ApiScannerConnectionPayload::class
            VERO_2_INFO_SNAPSHOT -> ApiVero2InfoSnapshotPayload::class
            SCANNER_FIRMWARE_UPDATE -> ApiScannerFirmwareUpdatePayload::class
            INVALID_INTENT -> ApiInvalidIntentPayload::class
            SUSPICIOUS_INTENT -> ApiSuspiciousIntentPayload::class
            INTENT_PARSING -> ApiIntentParsingPayload::class
            COMPLETION_CHECK -> ApiCompletionCheckPayload::class
            SESSION_CAPTURE -> ApiSessionCapturePayload::class
            FACE_ONBOARDING_COMPLETE -> ApiFaceOnboardingCompletePayload::class
            FACE_FALLBACK_CAPTURE -> ApiFaceFallbackCapturePayload::class
            FACE_CAPTURE -> ApiFaceCapturePayload::class
            FACE_CAPTURE_CONFIRMATION -> ApiFaceCaptureConfirmationPayload::class
            FACE_CAPTURE_RETRY -> ApiFaceCaptureRetryPayload::class
        }
    }
}
