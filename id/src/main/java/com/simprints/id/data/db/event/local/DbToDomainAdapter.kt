package com.simprints.id.data.db.event.local

import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.domain.events.*
import com.simprints.id.data.db.event.domain.events.EventPayloadType.*
import com.simprints.id.data.db.event.domain.events.callback.*
import com.simprints.id.data.db.event.domain.events.callout.*
import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent
import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent
import com.simprints.id.data.db.event.local.models.DbEvent

fun DbEvent.toDomainEvent(): Event? =
    jsonEvent?.let {
        when (getType()) {
            ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(it, ArtificialTerminationEvent::class.java)
            REFUSAL -> JsonHelper.gson.fromJson(it, RefusalEvent::class.java)
            CONSENT -> JsonHelper.gson.fromJson(it, ConsentEvent::class.java)
            ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentEvent::class.java)
            ALERT_SCREEN -> JsonHelper.gson.fromJson(it, AlertScreenEvent::class.java)
            CANDIDATE_READ -> JsonHelper.gson.fromJson(it, CandidateReadEvent::class.java)
            AUTHORIZATION -> JsonHelper.gson.fromJson(it, AuthorizationEvent::class.java)
            GUID_SELECTION -> JsonHelper.gson.fromJson(it, GuidSelectionEvent::class.java)
            AUTHENTICATION -> JsonHelper.gson.fromJson(it, AuthenticationEvent::class.java)
            ONE_TO_ONE_MATCH -> JsonHelper.gson.fromJson(it, OneToOneMatchEvent::class.java)
            PERSON_CREATION -> JsonHelper.gson.fromJson(it, PersonCreationEvent::class.java)
            ONE_TO_MANY_MATCH -> JsonHelper.gson.fromJson(it, OneToManyMatchEvent::class.java)
            SCANNER_CONNECTION -> JsonHelper.gson.fromJson(it, ScannerConnectionEvent::class.java)
            FINGERPRINT_CAPTURE -> JsonHelper.gson.fromJson(it, FingerprintCaptureEvent::class.java)
            CONNECTIVITY_SNAPSHOT -> JsonHelper.gson.fromJson(it, ConnectivitySnapshotEvent::class.java)
            INVALID_INTENT -> JsonHelper.gson.fromJson(it, InvalidIntentEvent::class.java)
            SUSPICIOUS_INTENT -> JsonHelper.gson.fromJson(it, SuspiciousIntentEvent::class.java)
            CALLOUT_CONFIRMATION -> JsonHelper.gson.fromJson(it, ConfirmationCalloutEvent::class.java)
            CALLOUT_IDENTIFICATION -> JsonHelper.gson.fromJson(it, IdentificationCalloutEvent::class.java)
            CALLOUT_ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentCalloutEvent::class.java)
            CALLOUT_VERIFICATION -> JsonHelper.gson.fromJson(it, VerificationCalloutEvent::class.java)
            CALLBACK_IDENTIFICATION -> JsonHelper.gson.fromJson(it, IdentificationCallbackEvent::class.java)
            CALLBACK_ENROLMENT -> JsonHelper.gson.fromJson(it, EnrolmentCallbackEvent::class.java)
            CALLBACK_VERIFICATION -> JsonHelper.gson.fromJson(it, VerificationCallbackEvent::class.java)
            CALLBACK_REFUSAL -> JsonHelper.gson.fromJson(it, RefusalCallbackEvent::class.java)
            CALLBACK_ERROR -> JsonHelper.gson.fromJson(it, ErrorCallbackEvent::class.java)
            INTENT_PARSING -> JsonHelper.gson.fromJson(it, IntentParsingEvent::class.java)
            COMPLETION_CHECK -> JsonHelper.gson.fromJson(it, CompletionCheckEvent::class.java)
            CALLBACK_CONFIRMATION -> JsonHelper.gson.fromJson(it, ConfirmationCallbackEvent::class.java)
            CALLOUT_LAST_BIOMETRICS -> JsonHelper.gson.fromJson(it, EnrolmentLastBiometricsCalloutEvent::class.java)
            VERO_2_INFO_SNAPSHOT -> JsonHelper.gson.fromJson(it, Vero2InfoSnapshotEvent::class.java)
            SCANNER_FIRMWARE_UPDATE -> JsonHelper.gson.fromJson(it, ScannerFirmwareUpdateEvent::class.java)
            ENROLMENT_RECORD_CREATION -> JsonHelper.gson.fromJson(it, EnrolmentRecordCreationEvent::class.java)
            ENROLMENT_RECORD_DELETION -> JsonHelper.gson.fromJson(it, EnrolmentRecordDeletionEvent::class.java)
            ENROLMENT_RECORD_MOVE -> JsonHelper.gson.fromJson(it, EnrolmentRecordMoveEvent::class.java)
            SESSION_CAPTURE -> JsonHelper.gson.fromJson(it, SessionCaptureEvent::class.java)

            null -> null
        }
    }

