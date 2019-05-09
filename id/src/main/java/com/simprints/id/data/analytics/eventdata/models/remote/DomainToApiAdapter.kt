package com.simprints.id.data.analytics.eventdata.models.remote

import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.EventType.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.EnrolmentCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.IdentificationCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.RefusalCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.VerificationCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.remote.events.*

fun Event.toApiEvent(): ApiEvent =
    when (this.type) {
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
        SUSPICIOUS_INTENT -> ApiSuspiciousIntentEvent(this as SuspiciousIntentEvent)
        CALLOUT_CONFIRMATION -> ApiCalloutEvent(this as ConfirmationCalloutEvent)
        CALLOUT_IDENTIFICATION ->  ApiCalloutEvent(this as IdentificationCalloutEvent)
        CALLOUT_ENROLMENT -> ApiCalloutEvent(this as EnrolmentCalloutEvent)
        CALLOUT_VERIFICATION ->  ApiCalloutEvent(this as VerificationCalloutEvent)
        CALLBACK_IDENTIFICATION -> ApiCallbackEvent(this as IdentificationCallbackEvent)
        CALLBACK_ENROLMENT -> ApiCallbackEvent(this as EnrolmentCallbackEvent)
        CALLBACK_REFUSAL -> ApiCallbackEvent(this as RefusalCallbackEvent)
        CALLBACK_VERIFICATION -> ApiCallbackEvent(this as VerificationCallbackEvent)
    }
