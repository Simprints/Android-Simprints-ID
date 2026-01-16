package com.simprints.infra.events.event.domain.models

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.AgeGroupSelectionEvent.AgeGroupSelectionPayload
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload
import com.simprints.infra.events.event.domain.models.AuthenticationEvent.AuthenticationPayload
import com.simprints.infra.events.event.domain.models.AuthorizationEvent.AuthorizationPayload
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent.BiometricReferenceCreationPayload
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload
import com.simprints.infra.events.event.domain.models.CompletionCheckEvent.CompletionCheckPayload
import com.simprints.infra.events.event.domain.models.ConnectivitySnapshotEvent.ConnectivitySnapshotPayload
import com.simprints.infra.events.event.domain.models.ConsentEvent.ConsentPayload
import com.simprints.infra.events.event.domain.models.EnrolmentUpdateEvent.EnrolmentUpdatePayload
import com.simprints.infra.events.event.domain.models.EventType.Companion
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureEvent.ExternalCredentialCapturePayload
import com.simprints.infra.events.event.domain.models.ExternalCredentialCaptureValueEvent.ExternalCredentialCaptureValuePayload
import com.simprints.infra.events.event.domain.models.ExternalCredentialConfirmationEvent.*
import com.simprints.infra.events.event.domain.models.ExternalCredentialSearchEvent.*
import com.simprints.infra.events.event.domain.models.ExternalCredentialSelectionEvent.*
import com.simprints.infra.events.event.domain.models.GuidSelectionEvent.GuidSelectionPayload
import com.simprints.infra.events.event.domain.models.IntentParsingEvent.IntentParsingPayload
import com.simprints.infra.events.event.domain.models.InvalidIntentEvent.InvalidIntentPayload
import com.simprints.infra.events.event.domain.models.LicenseCheckEvent.LicenseCheckEventPayload
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload
import com.simprints.infra.events.event.domain.models.PersonCreationEvent.PersonCreationPayload
import com.simprints.infra.events.event.domain.models.RefusalEvent.RefusalPayload
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent.ScannerConnectionPayload
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent.ScannerFirmwareUpdatePayload
import com.simprints.infra.events.event.domain.models.SuspiciousIntentEvent.SuspiciousIntentPayload
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.infra.events.event.domain.models.ConfirmationCallbackEvent.ConfirmationCallbackPayload
import com.simprints.infra.events.event.domain.models.EnrolmentCallbackEvent.EnrolmentCallbackPayload
import com.simprints.infra.events.event.domain.models.ErrorCallbackEvent.ErrorCallbackPayload
import com.simprints.infra.events.event.domain.models.IdentificationCallbackEvent.IdentificationCallbackPayload
import com.simprints.infra.events.event.domain.models.RefusalCallbackEvent.RefusalCallbackPayload
import com.simprints.infra.events.event.domain.models.VerificationCallbackEvent.VerificationCallbackPayload
import com.simprints.infra.events.event.domain.models.EventDownSyncRequestEvent.EventDownSyncRequestPayload
import com.simprints.infra.events.event.domain.models.FaceCaptureBiometricsEvent.FaceCaptureBiometricsPayload
import com.simprints.infra.events.event.domain.models.FaceCaptureConfirmationEvent.FaceCaptureConfirmationPayload
import com.simprints.infra.events.event.domain.models.FaceCaptureEvent.FaceCapturePayload
import com.simprints.infra.events.event.domain.models.FaceFallbackCaptureEvent.FaceFallbackCapturePayload
import com.simprints.infra.events.event.domain.models.FaceOnboardingCompleteEvent.FaceOnboardingCompletePayload
import com.simprints.infra.events.event.domain.models.FingerprintCaptureBiometricsEvent.FingerprintCaptureBiometricsPayload
import com.simprints.infra.events.event.domain.models.FingerprintCaptureEvent.FingerprintCapturePayload
import com.simprints.infra.events.event.domain.models.SampleUpSyncRequestEvent.SampleUpSyncRequestPayload
import com.simprints.infra.events.event.domain.models.EventUpSyncRequestEvent.EventUpSyncRequestPayload

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(value = ConfirmationCallbackPayload::class, name = EventType.CALLBACK_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = EnrolmentCallbackPayload::class, name = EventType.CALLBACK_ENROLMENT_KEY),
    JsonSubTypes.Type(value = ErrorCallbackPayload::class, name = EventType.CALLBACK_ERROR_KEY),
    JsonSubTypes.Type(value = IdentificationCallbackPayload::class, name = EventType.CALLBACK_IDENTIFICATION_KEY),
    JsonSubTypes.Type(value = RefusalCallbackPayload::class, name = EventType.CALLBACK_REFUSAL_KEY),
    JsonSubTypes.Type(value = VerificationCallbackPayload::class, name = EventType.CALLBACK_VERIFICATION_KEY),
    JsonSubTypes.Type(value = ConfirmationCalloutEventV2.ConfirmationCalloutPayload::class, name = EventType.CALLOUT_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = ConfirmationCalloutEventV3.ConfirmationCalloutPayload::class, name = EventType.CALLOUT_CONFIRMATION_V3_KEY),
    JsonSubTypes.Type(value = EnrolmentCalloutEventV2.EnrolmentCalloutPayload::class, name = EventType.CALLOUT_ENROLMENT_KEY),
    JsonSubTypes.Type(value = EnrolmentCalloutEventV3.EnrolmentCalloutPayload::class, name = EventType.CALLOUT_ENROLMENT_V3_KEY),
    JsonSubTypes.Type(
        value = EnrolmentLastBiometricsCalloutEventV2.EnrolmentLastBiometricsCalloutPayload::class,
        name = EventType.CALLOUT_LAST_BIOMETRICS_KEY,
    ),
    JsonSubTypes.Type(
        value = EnrolmentLastBiometricsCalloutEventV3.EnrolmentLastBiometricsCalloutPayload::class,
        name = EventType.CALLOUT_LAST_BIOMETRICS_V3_KEY,
    ),
    JsonSubTypes.Type(
        value = IdentificationCalloutEventV2.IdentificationCalloutPayload::class,
        name = EventType.CALLOUT_IDENTIFICATION_KEY,
    ),
    JsonSubTypes.Type(
        value = IdentificationCalloutEventV3.IdentificationCalloutPayload::class,
        name = EventType.CALLOUT_IDENTIFICATION_V3_KEY,
    ),
    JsonSubTypes.Type(value = VerificationCalloutEventV2.VerificationCalloutPayload::class, name = EventType.CALLOUT_VERIFICATION_KEY),
    JsonSubTypes.Type(value = VerificationCalloutEventV3.VerificationCalloutPayload::class, name = EventType.CALLOUT_VERIFICATION_V3_KEY),
    JsonSubTypes.Type(value = FaceCaptureConfirmationPayload::class, name = EventType.FACE_CAPTURE_CONFIRMATION_KEY),
    JsonSubTypes.Type(value = FaceCapturePayload::class, name = EventType.FACE_CAPTURE_KEY),
    JsonSubTypes.Type(value = FaceCaptureBiometricsPayload::class, name = EventType.FACE_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = FaceFallbackCapturePayload::class, name = EventType.FACE_FALLBACK_CAPTURE_KEY),
    JsonSubTypes.Type(value = FaceOnboardingCompletePayload::class, name = EventType.FACE_ONBOARDING_COMPLETE_KEY),
    JsonSubTypes.Type(value = AlertScreenPayload::class, name = EventType.ALERT_SCREEN_KEY),
    JsonSubTypes.Type(value = AuthenticationPayload::class, name = EventType.AUTHENTICATION_KEY),
    JsonSubTypes.Type(value = AuthorizationPayload::class, name = EventType.AUTHORIZATION_KEY),
    JsonSubTypes.Type(value = CandidateReadPayload::class, name = EventType.CANDIDATE_READ_KEY),
    JsonSubTypes.Type(value = CompletionCheckPayload::class, name = EventType.COMPLETION_CHECK_KEY),
    JsonSubTypes.Type(value = ConnectivitySnapshotPayload::class, name = EventType.CONNECTIVITY_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = ConsentPayload::class, name = EventType.CONSENT_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV2.EnrolmentPayload::class, name = EventType.ENROLMENT_V2_KEY),
    JsonSubTypes.Type(value = EnrolmentEventV4.EnrolmentPayload::class, name = EventType.ENROLMENT_V4_KEY),
    JsonSubTypes.Type(value = FingerprintCapturePayload::class, name = EventType.FINGERPRINT_CAPTURE_KEY),
    JsonSubTypes.Type(value = FingerprintCaptureBiometricsPayload::class, name = EventType.FINGERPRINT_CAPTURE_BIOMETRICS_KEY),
    JsonSubTypes.Type(value = GuidSelectionPayload::class, name = EventType.GUID_SELECTION_KEY),
    JsonSubTypes.Type(value = IntentParsingPayload::class, name = EventType.INTENT_PARSING_KEY),
    JsonSubTypes.Type(value = InvalidIntentPayload::class, name = EventType.INVALID_INTENT_KEY),
    JsonSubTypes.Type(value = OneToManyMatchPayload::class, name = EventType.ONE_TO_MANY_MATCH_KEY),
    JsonSubTypes.Type(value = OneToOneMatchPayload::class, name = EventType.ONE_TO_ONE_MATCH_KEY),
    JsonSubTypes.Type(value = PersonCreationPayload::class, name = EventType.PERSON_CREATION_KEY),
    JsonSubTypes.Type(value = RefusalPayload::class, name = EventType.REFUSAL_KEY),
    JsonSubTypes.Type(value = ScannerConnectionPayload::class, name = EventType.SCANNER_CONNECTION_KEY),
    JsonSubTypes.Type(value = ScannerFirmwareUpdatePayload::class, name = Companion.SCANNER_FIRMWARE_UPDATE_KEY),
    JsonSubTypes.Type(value = Vero2InfoSnapshotPayload::class, name = EventType.VERO_2_INFO_SNAPSHOT_KEY),
    JsonSubTypes.Type(value = SuspiciousIntentPayload::class, name = EventType.SUSPICIOUS_INTENT_KEY),
    JsonSubTypes.Type(value = EventDownSyncRequestPayload::class, name = Companion.EVENT_DOWN_SYNC_REQUEST_KEY),
    JsonSubTypes.Type(value = EventUpSyncRequestPayload::class, name = Companion.EVENT_UP_SYNC_REQUEST_KEY),
    JsonSubTypes.Type(value = LicenseCheckEventPayload::class, name = Companion.LICENSE_CHECK_KEY),
    JsonSubTypes.Type(value = AgeGroupSelectionPayload::class, name = Companion.AGE_GROUP_SELECTION_KEY),
    JsonSubTypes.Type(value = BiometricReferenceCreationPayload::class, name = Companion.BIOMETRIC_REFERENCE_CREATION_KEY),
    JsonSubTypes.Type(value = SampleUpSyncRequestPayload::class, name = Companion.SAMPLE_UP_SYNC_REQUEST),
    JsonSubTypes.Type(value = EnrolmentUpdatePayload::class, name = Companion.ENROLMENT_UPDATE_KEY),
    JsonSubTypes.Type(value = ExternalCredentialSelectionPayload::class, name = Companion.EXTERNAL_CREDENTIAL_SELECTION_KEY),
    JsonSubTypes.Type(value = ExternalCredentialCaptureValuePayload::class, name = Companion.EXTERNAL_CREDENTIAL_CAPTURE_VALUE_KEY),
    JsonSubTypes.Type(value = ExternalCredentialCapturePayload::class, name = Companion.EXTERNAL_CREDENTIAL_CAPTURE_KEY),
    JsonSubTypes.Type(value = ExternalCredentialSearchPayload::class, name = Companion.EXTERNAL_CREDENTIAL_SEARCH_KEY),
    JsonSubTypes.Type(value = ExternalCredentialConfirmationPayload::class, name = Companion.EXTERNAL_CREDENTIAL_CONFIRMATION_KEY),
)
abstract class EventPayload {
    abstract val type: EventType
    abstract val eventVersion: Int
    abstract val createdAt: Timestamp
    abstract val endedAt: Timestamp?

    open fun toSafeString(): String = ""
}
