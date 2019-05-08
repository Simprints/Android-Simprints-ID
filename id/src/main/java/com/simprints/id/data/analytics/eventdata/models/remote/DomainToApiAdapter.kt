package com.simprints.id.data.analytics.eventdata.models.remote

import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.*
import com.simprints.id.data.analytics.eventdata.models.remote.events.*

fun Event.toApiEvent(): ApiEvent = when (this.type) {
    ARTIFICIAL_TERMINATION -> ApiArtificialTerminationEvent(this as ArtificialTerminationEvent)
    REFUSAL -> ApiRefusalEvent(this as RefusalEvent)
    CONSENT -> ApiConsentEvent(this as ConsentEvent)
    ENROLMENT -> ApiEnrollmentEvent(this as EnrolmentEvent)
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
    CALLOUT -> ApiCalloutEvent(this as CalloutEvent)
    CALLBACK -> ApiCallbackEvent(this as CallbackEvent)
    SUSPICIOUS_INTENT -> ApiSuspiciousIntentEvent(this as SuspiciousIntentEvent)
}
