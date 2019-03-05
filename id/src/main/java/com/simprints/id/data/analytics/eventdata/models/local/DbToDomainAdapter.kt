package com.simprints.id.data.analytics.eventdata.models.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.analytics.eventdata.models.domain.events.*

fun RlEvent.toDomainEvent(): Event? = when (getType()) {
    EventType.ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(jsonEvent, ArtificialTerminationEvent::class.java)
    EventType.REFUSAL -> JsonHelper.gson.fromJson(jsonEvent, RefusalEvent::class.java)
    EventType.CONSENT -> JsonHelper.gson.fromJson(jsonEvent, ConsentEvent::class.java)
    EventType.ENROL_REQUEST -> JsonHelper.gson.fromJson(jsonEvent, EnrolRequestEvent::class.java)
    EventType.VERIFY_REQUEST -> JsonHelper.gson.fromJson(jsonEvent, VerifyRequestEvent::class.java)
    EventType.IDENTIFY_REQUEST -> JsonHelper.gson.fromJson(jsonEvent, IdentifyRequestEvent::class.java)
    EventType.IDENTIFY_CONFIRMATION_REQUEST -> JsonHelper.gson.fromJson(jsonEvent, IdentifyConfirmationRequestEvent::class.java)
    EventType.ENROL_RESPONSE -> JsonHelper.gson.fromJson(jsonEvent, EnrolResponseEvent::class.java)
    EventType.VERIFY_RESPONSE -> JsonHelper.gson.fromJson(jsonEvent, VerifyResponseEvent::class.java)
    EventType.IDENTIFY_RESPONSE -> JsonHelper.gson.fromJson(jsonEvent, IdentifyResponseEvent::class.java)
    EventType.ENROLLMENT -> JsonHelper.gson.fromJson(jsonEvent, EnrollmentEvent::class.java)
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
    null -> null
}
