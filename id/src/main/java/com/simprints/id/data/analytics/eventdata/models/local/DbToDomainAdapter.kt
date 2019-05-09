package com.simprints.id.data.analytics.eventdata.models.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.eventdata.models.domain.events.*
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.EnrolmentCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.IdentificationCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.RefusalCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callback.VerificationCallbackEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.ConfirmationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.EnrolmentCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.IdentificationCalloutEvent
import com.simprints.id.data.analytics.eventdata.models.domain.events.callout.VerificationCalloutEvent

fun DbEvent.toDomainEvent(): Event? =
    when (getType()) {
        EventType.ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(jsonEvent, ArtificialTerminationEvent::class.java)
        EventType.REFUSAL -> JsonHelper.gson.fromJson(jsonEvent, RefusalEvent::class.java)
        EventType.CONSENT -> JsonHelper.gson.fromJson(jsonEvent, ConsentEvent::class.java)
        EventType.ENROLMENT -> JsonHelper.gson.fromJson(jsonEvent, EnrolmentEvent::class.java)
        EventType.ALERT_SCREEN -> JsonHelper.gson.fromJson(jsonEvent, AlertScreenEvent::class.java)
        EventType.CANDIDATE_READ -> JsonHelper.gson.fromJson(jsonEvent, CandidateReadEvent::class.java)
        EventType.AUTHORIZATION -> JsonHelper.gson.fromJson(jsonEvent, AuthorizationEvent::class.java)
        EventType.GUID_SELECTION -> JsonHelper.gson.fromJson(jsonEvent, GuidSelectionEvent::class.java)
        EventType.AUTHENTICATION -> JsonHelper.gson.fromJson(jsonEvent, AuthenticationEvent::class.java)
        EventType.ONE_TO_ONE_MATCH -> JsonHelper.gson.fromJson(jsonEvent, OneToOneMatchEvent::class.java)
        EventType.PERSON_CREATION -> JsonHelper.gson.fromJson(jsonEvent, PersonCreationEvent::class.java)
        EventType.ONE_TO_MANY_MATCH -> JsonHelper.gson.fromJson(jsonEvent, OneToManyMatchEvent::class.java)
        EventType.SCANNER_CONNECTION -> JsonHelper.gson.fromJson(jsonEvent, ScannerConnectionEvent::class.java)
        EventType.FINGERPRINT_CAPTURE -> JsonHelper.gson.fromJson(jsonEvent, FingerprintCaptureEvent::class.java)
        EventType.CONNECTIVITY_SNAPSHOT -> JsonHelper.gson.fromJson(jsonEvent, ConnectivitySnapshotEvent::class.java)
        EventType.INVALID_INTENT -> JsonHelper.gson.fromJson(jsonEvent, InvalidIntentEvent::class.java)
        EventType.SUSPICIOUS_INTENT -> JsonHelper.gson.fromJson(jsonEvent, SuspiciousIntentEvent::class.java)
        EventType.CALLOUT_CONFIRMATION -> JsonHelper.gson.fromJson(jsonEvent, ConfirmationCalloutEvent::class.java)
        EventType.CALLOUT_IDENTIFICATION -> JsonHelper.gson.fromJson(jsonEvent, IdentificationCalloutEvent::class.java)
        EventType.CALLOUT_ENROLMENT -> JsonHelper.gson.fromJson(jsonEvent, EnrolmentCalloutEvent::class.java)
        EventType.CALLOUT_VERIFICATION -> JsonHelper.gson.fromJson(jsonEvent, VerificationCalloutEvent::class.java)
        EventType.CALLBACK_IDENTIFICATION -> JsonHelper.gson.fromJson(jsonEvent, IdentificationCallbackEvent::class.java)
        EventType.CALLBACK_ENROLMENT -> JsonHelper.gson.fromJson(jsonEvent, EnrolmentCallbackEvent::class.java)
        EventType.CALLBACK_VERIFICATION -> JsonHelper.gson.fromJson(jsonEvent, VerificationCallbackEvent::class.java)
        EventType.CALLBACK_REFUSAL -> JsonHelper.gson.fromJson(jsonEvent, RefusalCallbackEvent::class.java)
        null -> null
    }
