package com.simprints.id.data.analytics.events.realm

import com.simprints.id.data.analytics.events.models.*
import com.simprints.id.data.analytics.events.models.EventType.*
import com.simprints.id.tools.json.JsonHelper
import io.realm.RealmObject

open class RlEvent : RealmObject {

    private var typeEventDescription: String? = null

    var jsonEvent: String? = null

    private fun saveType(type: EventType) {
        this.typeEventDescription = type.toString()
    }

    private fun getType(): EventType? = typeEventDescription?.let { valueOf(it) }

    constructor() {}
    constructor(event: Event) : this() {
        saveType(event.type)
        jsonEvent = JsonHelper.toJson(event)
    }

    fun getEvent(): Event? {
        return when (getType()) {
            REFUSAL -> JsonHelper.gson.fromJson(jsonEvent, RefusalEvent::class.java)
            CONSENT -> JsonHelper.gson.fromJson(jsonEvent, ConsentEvent::class.java)
            CALLOUT -> JsonHelper.gson.fromJson(jsonEvent, CalloutEvent::class.java)
            CALLBACK -> JsonHelper.gson.fromJson(jsonEvent, CallbackEvent::class.java)
            ENROLLMENT -> JsonHelper.gson.fromJson(jsonEvent, EnrollmentEvent::class.java)
            ALERT_SCREEN -> JsonHelper.gson.fromJson(jsonEvent, AlertScreenEvent::class.java)
            CANDIDATE_READ -> JsonHelper.gson.fromJson(jsonEvent, CandidateReadEvent::class.java)
            AUTHORIZATION -> JsonHelper.gson.fromJson(jsonEvent, AuthorizationEvent::class.java)
            GUID_SELECTION -> JsonHelper.gson.fromJson(jsonEvent, GuidSelectionEvent::class.java)
            AUTHENTICATION -> JsonHelper.gson.fromJson(jsonEvent, AuthenticationEvent::class.java)
            ONE_TO_ONE_MATCH -> JsonHelper.gson.fromJson(jsonEvent, OneToOneMatchEvent::class.java)
            PERSON_CREATION -> JsonHelper.gson.fromJson(jsonEvent, PersonCreationEvent::class.java)
            ONE_TO_MANY_MATCH -> JsonHelper.gson.fromJson(jsonEvent, OneToManyMatchEvent::class.java)
            SCANNER_CONNECTION -> JsonHelper.gson.fromJson(jsonEvent, ScannerConnectionEvent::class.java)
            FINGERPRINT_CAPTURE -> JsonHelper.gson.fromJson(jsonEvent, FingerprintCaptureEvent::class.java)
            CONNECTIVITY_SNAPSHOT -> JsonHelper.gson.fromJson(jsonEvent, ConnectivitySnapshotEvent::class.java)
            ARTIFICIAL_TERMINATION -> JsonHelper.gson.fromJson(jsonEvent, ArtificialTerminationEvent::class.java)

            null -> null
        }
    }
}
