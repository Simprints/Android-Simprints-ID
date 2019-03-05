package com.simprints.id.data.analytics.eventdata.models.remote

import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.*

fun Event.toApiEvent(): ApiEvent = when (this.type) {
    ARTIFICIAL_TERMINATION -> ApiArtificialTerminationEvent(this as ArtificialTerminationEvent)
    REFUSAL -> ApiRefusalEvent(this as RefusalEvent)
    CONSENT -> ApiConsentEvent(this as ConsentEvent)
    ENROL_REQUEST -> ApiCalloutEvent(this as EnrolRequestEvent)
    VERIFY_REQUEST -> ApiCalloutEvent(this as VerifyRequestEvent)
    IDENTIFY_REQUEST -> ApiCalloutEvent(this as IdentifyRequestEvent)
    IDENTIFY_CONFIRMATION_REQUEST -> ApiCalloutEvent(this as IdentifyConfirmationRequestEvent)
    ENROL_RESPONSE -> ApiCallbackEvent(this as EnrolResponseEvent)
    VERIFY_RESPONSE -> ApiCallbackEvent(this as VerifyResponseEvent)
    IDENTIFY_RESPONSE -> ApiCallbackEvent(this as IdentifyResponseEvent)
    REFUSAL_RESPONSE -> ApiCallbackEvent(this as RefusalFormResponseEvent)
    NO_RESPONSE -> ApiCallbackEvent(this as NoResponseEvent)
    ENROLLMENT -> ApiEnrollmentEvent(this as EnrollmentEvent)
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
}
