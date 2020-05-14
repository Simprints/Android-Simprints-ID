package com.simprints.id.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.subject.remote.models.subjectevents.*
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiBiometricReferenceType.FACE_REFERENCE
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiBiometricReferenceType.FINGERPRINT_REFERENCE
import com.simprints.id.data.db.subject.remote.models.subjectevents.ApiEventPayloadType.*

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
