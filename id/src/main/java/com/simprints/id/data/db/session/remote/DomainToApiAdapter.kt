package com.simprints.id.data.db.session.remote

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.*
import com.simprints.id.data.db.session.domain.models.events.EventType.*
import com.simprints.id.data.db.session.domain.models.events.callback.*
import com.simprints.id.data.db.session.domain.models.events.callout.*
import com.simprints.id.data.db.session.remote.events.*

@Keep
fun Event.toApiEvent(): ApiEvent =
    when (this.type) {
        ARTIFICIAL_TERMINATION -> ApiArtificialTerminationEvent(this as ArtificialTerminationEvent)
        REFUSAL -> ApiRefusalEvent(this as RefusalEvent)
        CONSENT -> ApiConsentEvent(this as ConsentEvent)
        ENROLMENT -> ApiEnrolmentEvent(this as EnrolmentEvent)
        ALERT_SCREEN ->  ApiAlertScreenEvent(this as AlertScreenEvent)
        CANDIDATE_READ -> ApiCandidateReadEvent(this as CandidateReadEvent)
        AUTHORIZATION -> ApiAuthorizationEvent(this as AuthorizationEvent)
        GUID_SELECTION -> ApiGuidSelectionEvent(this as GuidSelectionEvent)
        AUTHENTICATION ->  ApiAuthenticationEvent(this as AuthenticationEvent)
        ONE_TO_ONE_MATCH -> ApiOneToOneMatchEvent(this as OneToOneMatchEvent)
        PERSON_CREATION ->  ApiPersonCreationEvent(this as PersonCreationEvent)
        ONE_TO_MANY_MATCH -> ApiOneToManyMatchEvent(this as OneToManyMatchEvent)
        SCANNER_CONNECTION -> ApiScannerConnectionEvent(this as ScannerConnectionEvent)
        FINGERPRINT_CAPTURE -> ApiFingerprintCaptureEvent(this as FingerprintCaptureEvent)
        CONNECTIVITY_SNAPSHOT -> ApiConnectivitySnapshotEvent(this as ConnectivitySnapshotEvent)
        INVALID_INTENT -> ApiInvalidIntentEvent(this as InvalidIntentEvent)
        SUSPICIOUS_INTENT -> ApiSuspiciousIntentEvent(this as SuspiciousIntentEvent)
        COMPLETION_CHECK -> ApiCompletionCheckEvent(this as CompletionCheckEvent)
        INTENT_PARSING -> ApiIntentParsingEvent(this as IntentParsingEvent)
        CALLOUT_CONFIRMATION -> ApiCalloutEvent(this as ConfirmationCalloutEvent)
        CALLOUT_IDENTIFICATION ->  ApiCalloutEvent(this as IdentificationCalloutEvent)
        CALLOUT_ENROLMENT -> ApiCalloutEvent(this as EnrolmentCalloutEvent)
        CALLOUT_VERIFICATION ->  ApiCalloutEvent(this as VerificationCalloutEvent)
        CALLOUT_LAST_BIOMETRICS -> ApiCalloutEvent(this as EnrolmentLastBiometricsCalloutEvent)
        CALLBACK_IDENTIFICATION -> ApiCallbackEvent(this as IdentificationCallbackEvent)
        CALLBACK_ENROLMENT -> ApiCallbackEvent(this as EnrolmentCallbackEvent)
        CALLBACK_REFUSAL -> ApiCallbackEvent(this as RefusalCallbackEvent)
        CALLBACK_VERIFICATION -> ApiCallbackEvent(this as VerificationCallbackEvent)
        CALLBACK_CONFIRMATION -> ApiCallbackEvent(this as ConfirmationCallbackEvent)
        CALLBACK_ERROR -> ApiCallbackEvent(this as ErrorCallbackEvent)
    }
