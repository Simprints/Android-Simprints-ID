package com.simprints.id.tools.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.person.remote.models.personevents.*
import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FaceReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiBiometricReferenceType.FingerprintReference
import com.simprints.id.data.db.person.remote.models.personevents.ApiEventPayloadType.*

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
            .registerSubtype(ApiEnrolmentRecordCreationPayload::class.java, EnrolmentRecordCreation.name)
            .registerSubtype(ApiEnrolmentRecordDeletionPayload::class.java, EnrolmentRecordDeletion.name)
            .registerSubtype(ApiEnrolmentRecordMovePayload::class.java, EnrolmentRecordMove.name)

    private fun getTypeAdapterFactoryForBiometricReferences() =
        RuntimeTypeAdapterFactory.of(ApiBiometricReference::class.java, "type")
            .registerSubtype(ApiFingerprintReference::class.java, FingerprintReference.name)
            .registerSubtype(ApiFaceReference::class.java, FaceReference.name)
}
