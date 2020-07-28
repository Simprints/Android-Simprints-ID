package com.simprints.id.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.event.remote.events.ApiEventPayload
import com.simprints.id.data.db.event.remote.events.ApiEventPayloadType.*
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReference
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceType.FACE_REFERENCE
import com.simprints.id.data.db.event.remote.events.subject.ApiBiometricReferenceType.FINGERPRINT_REFERENCE
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordCreationPayload.ApiEnrolmentRecordCreationPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordDeletionEvent.ApiEnrolmentRecordDeletionPayload
import com.simprints.id.data.db.event.remote.events.subject.ApiEnrolmentRecordMoveEvent.ApiEnrolmentRecordMovePayload
import com.simprints.id.data.db.event.remote.events.subject.ApiFaceReference
import com.simprints.id.data.db.event.remote.events.subject.ApiFingerprintReference

object SimJsonHelper {
    private val gsonBuilder: GsonBuilder by lazy {
        JsonHelper.defaultBuilder
            .registerTypeAdapterFactory(getTypeAdapterFactoryForEvents())
            .registerTypeAdapterFactory(getTypeAdapterFactoryForBiometricReferences())
    }

    val gson: Gson by lazy {
        gsonBuilder.create()
    }

    private fun getTypeAdapterFactoryForEvents() =
        RuntimeTypeAdapterFactory.of(ApiEventPayload::class.java, "type")
            .registerSubtype(ApiEnrolmentRecordCreationPayload::class.java, ENROLMENT_RECORD_CREATION.apiName)
            .registerSubtype(ApiEnrolmentRecordDeletionPayload::class.java, ENROLMENT_RECORD_DELETION.apiName)
            .registerSubtype(ApiEnrolmentRecordMovePayload::class.java, ENROLMENT_RECORD_MOVE.apiName)

    private fun getTypeAdapterFactoryForBiometricReferences() =
        RuntimeTypeAdapterFactory.of(ApiBiometricReference::class.java, "type")
            .registerSubtype(ApiFingerprintReference::class.java, FINGERPRINT_REFERENCE.apiName)
            .registerSubtype(ApiFaceReference::class.java, FACE_REFERENCE.apiName)
}
