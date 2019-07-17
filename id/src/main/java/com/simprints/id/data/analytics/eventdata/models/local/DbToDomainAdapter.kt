package com.simprints.id.data.analytics.eventdata.models.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCalloutEvent

fun DbEvent.toDomainEvent(): Event? =
    jsonEvent?.let {
        when (getType()) {
            EventType.ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(it, ArtificialTerminationEvent::class.java)
            EventType.REFUSAL -> JsonHelper.gson.fromJson(it, RefusalEvent::class.java)
            EventType.CONSENT -> JsonHelper.gson.fromJson(it, ConsentEvent::class.java)
            EventType.ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentEvent::class.java)
            EventType.ALERT_SCREEN -> JsonHelper.gson.fromJson(it, AlertScreenEvent::class.java)
            EventType.CANDIDATE_READ -> JsonHelper.gson.fromJson(it, CandidateReadEvent::class.java)
            EventType.AUTHORIZATION -> JsonHelper.gson.fromJson(it, AuthorizationEvent::class.java)
            EventType.GUID_SELECTION -> JsonHelper.gson.fromJson(it, GuidSelectionEvent::class.java)
            EventType.AUTHENTICATION -> JsonHelper.gson.fromJson(it, AuthenticationEvent::class.java)
            EventType.ONE_TO_ONE_MATCH -> JsonHelper.gson.fromJson(it, OneToOneMatchEvent::class.java)
            EventType.PERSON_CREATION -> JsonHelper.gson.fromJson(it, PersonCreationEvent::class.java)
            EventType.ONE_TO_MANY_MATCH -> JsonHelper.gson.fromJson(it, OneToManyMatchEvent::class.java)
            EventType.SCANNER_CONNECTION -> JsonHelper.gson.fromJson(it, ScannerConnectionEvent::class.java)
            EventType.FINGERPRINT_CAPTURE -> JsonHelper.gson.fromJson(it, FingerprintCaptureEvent::class.java)
            EventType.CONNECTIVITY_SNAPSHOT -> JsonHelper.gson.fromJson(it, ConnectivitySnapshotEvent::class.java)
            EventType.INVALID_INTENT -> JsonHelper.gson.fromJson(it, InvalidIntentEvent::class.java)
            EventType.SUSPICIOUS_INTENT -> JsonHelper.gson.fromJson(it, SuspiciousIntentEvent::class.java)
            EventType.CALLOUT_CONFIRMATION -> JsonHelper.gson.fromJson(it, ConfirmationCalloutEvent::class.java)
            EventType.CALLOUT_IDENTIFICATION -> JsonHelper.gson.fromJson(it, IdentificationCalloutEvent::class.java)
            EventType.CALLOUT_ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentCalloutEvent::class.java)
            EventType.CALLOUT_VERIFICATION -> JsonHelper.gson.fromJson(it, VerificationCalloutEvent::class.java)
            EventType.CALLBACK_IDENTIFICATION -> JsonHelper.gson.fromJson(it, IdentificationCallbackEvent::class.java)
            EventType.CALLBACK_ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentCallbackEvent::class.java)
            EventType.CALLBACK_VERIFICATION -> JsonHelper.gson.fromJson(it, VerificationCallbackEvent::class.java)
            EventType.CALLBACK_REFUSAL -> JsonHelper.gson.fromJson(it, RefusalCallbackEvent::class.java)
            EventType.CALLBACK_ERROR -> JsonHelper.gson.fromJson(it, ErrorCallbackEvent::class.java)
            EventType.INTENT_PARSING -> JsonHelper.gson.fromJson(it, IntentParsingEvent::class.java)
            EventType.SKIP_CHECK -> JsonHelper.gson.fromJson(it, SkipCheckEvent::class.java)
            EventType.CALLBACK_CONFIRMATION -> JsonHelper.gson.fromJson(it, ConfirmationEvent::class.java)
            null -> null
        }
    }

