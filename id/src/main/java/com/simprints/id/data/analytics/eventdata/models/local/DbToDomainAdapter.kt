package com.simprints.id.data.analytics.eventdata.models.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.eventdata.models.domain.events.*

fun DbEvent.toDomainEvent(): Event? = when (getType()) {
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
    EventType.CALLOUT -> JsonHelper.gson.fromJson(jsonEvent, CalloutEvent::class.java)
    EventType.CALLBACK -> JsonHelper.gson.fromJson(jsonEvent, CallbackEvent::class.java )
    EventType.SUSPICIOUS_INTENT -> JsonHelper.gson.fromJson(jsonEvent, SuspiciousIntentEvent::class.java)
    null -> null
}
