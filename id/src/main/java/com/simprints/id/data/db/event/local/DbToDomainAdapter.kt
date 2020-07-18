//package com.simprints.id.data.db.event.local
//
//import com.simprints.core.tools.json.JsonHelper
//import com.simprints.id.data.db.event.domain.events.*
//import com.simprints.id.data.db.event.domain.events.EventType.*
//import com.simprints.id.data.db.event.domain.events.callback.*
//import com.simprints.id.data.db.event.domain.events.callout.*
//import com.simprints.id.data.db.event.domain.events.session.SessionCaptureEvent
//import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordCreationEvent
//import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordDeletionEvent
//import com.simprints.id.data.db.event.domain.events.subject.EnrolmentRecordMoveEvent
//import com.simprints.id.data.db.event.local.models.DbEvent
//import com.squareup.moshi.Moshi
//import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
//
//
//fun DbEvent.fromDbToDomain(): Event =
//    when (type) {
//        ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(eventJson, ArtificialTerminationEvent::class.java)
//        REFUSAL -> JsonHelper.gson.fromJson(eventJson, RefusalEvent::class.java)
//        CONSENT -> JsonHelper.gson.fromJson(eventJson, ConsentEvent::class.java)
//        ENROLMENT -> JsonHelper.gson.fromJson(eventJson, EnrolmentEvent::class.java)
//        ALERT_SCREEN -> JsonHelper.gson.fromJson(eventJson, AlertScreenEvent::class.java)
//        CANDIDATE_READ -> JsonHelper.gson.fromJson(eventJson, CandidateReadEvent::class.java)
//        AUTHORIZATION -> JsonHelper.gson.fromJson(eventJson, AuthorizationEvent::class.java)
//        GUID_SELECTION -> JsonHelper.gson.fromJson(eventJson, GuidSelectionEvent::class.java)
//        AUTHENTICATION -> JsonHelper.gson.fromJson(eventJson, AuthenticationEvent::class.java)
//        ONE_TO_ONE_MATCH -> JsonHelper.gson.fromJson(eventJson, OneToOneMatchEvent::class.java)
//        PERSON_CREATION -> JsonHelper.gson.fromJson(eventJson, PersonCreationEvent::class.java)
//        ONE_TO_MANY_MATCH -> JsonHelper.gson.fromJson(eventJson, OneToManyMatchEvent::class.java)
//        SCANNER_CONNECTION -> JsonHelper.gson.fromJson(eventJson, ScannerConnectionEvent::class.java)
//        FINGERPRINT_CAPTURE -> JsonHelper.gson.fromJson(eventJson, FingerprintCaptureEvent::class.java)
//        CONNECTIVITY_SNAPSHOT -> JsonHelper.gson.fromJson(eventJson, ConnectivitySnapshotEvent::class.java)
//        INVALID_INTENT -> JsonHelper.gson.fromJson(eventJson, InvalidIntentEvent::class.java)
//        SUSPICIOUS_INTENT -> JsonHelper.gson.fromJson(eventJson, SuspiciousIntentEvent::class.java)
//        CALLOUT_CONFIRMATION -> JsonHelper.gson.fromJson(eventJson, ConfirmationCalloutEvent::class.java)
//        CALLOUT_IDENTIFICATION -> JsonHelper.gson.fromJson(eventJson, IdentificationCalloutEvent::class.java)
//        CALLOUT_ENROLMENT -> JsonHelper.gson.fromJson(eventJson, EnrolmentCalloutEvent::class.java)
//        CALLOUT_VERIFICATION -> JsonHelper.gson.fromJson(eventJson, VerificationCalloutEvent::class.java)
//        CALLBACK_IDENTIFICATION -> JsonHelper.gson.fromJson(eventJson, IdentificationCallbackEvent::class.java)
//        CALLBACK_ENROLMENT -> JsonHelper.gson.fromJson(eventJson, EnrolmentCallbackEvent::class.java)
//        CALLBACK_VERIFICATION -> JsonHelper.gson.fromJson(eventJson, VerificationCallbackEvent::class.java)
//        CALLBACK_REFUSAL -> JsonHelper.gson.fromJson(eventJson, RefusalCallbackEvent::class.java)
//        CALLBACK_ERROR -> JsonHelper.gson.fromJson(eventJson, ErrorCallbackEvent::class.java)
//        INTENT_PARSING -> JsonHelper.gson.fromJson(eventJson, IntentParsingEvent::class.java)
//        COMPLETION_CHECK -> JsonHelper.gson.fromJson(eventJson, CompletionCheckEvent::class.java)
//        CALLBACK_CONFIRMATION -> JsonHelper.gson.fromJson(eventJson, ConfirmationCallbackEvent::class.java)
//        CALLOUT_LAST_BIOMETRICS -> JsonHelper.gson.fromJson(eventJson, EnrolmentLastBiometricsCalloutEvent::class.java)
//        VERO_2_INFO_SNAPSHOT -> JsonHelper.gson.fromJson(eventJson, Vero2InfoSnapshotEvent::class.java)
//        SCANNER_FIRMWARE_UPDATE -> JsonHelper.gson.fromJson(eventJson, ScannerFirmwareUpdateEvent::class.java)
//        ENROLMENT_RECORD_CREATION -> JsonHelper.gson.fromJson(eventJson, EnrolmentRecordCreationEvent::class.java)
//        ENROLMENT_RECORD_DELETION -> JsonHelper.gson.fromJson(eventJson, EnrolmentRecordDeletionEvent::class.java)
//        ENROLMENT_RECORD_MOVE -> JsonHelper.gson.fromJson(eventJson, EnrolmentRecordMoveEvent::class.java)
//        SESSION_CAPTURE -> {
//            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//            val jsonAdapter = moshi.adapter(SessionCaptureEvent::class.java)
//            jsonAdapter.fromJson(eventJson) as SessionCaptureEvent
//        }
//    }
