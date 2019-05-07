package com.simprints.id.data.db.remote.models

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import com.simprints.core.tools.json.PostGsonProcessable
import com.simprints.core.tools.json.SkipSerialisationProperty
import com.simprints.id.FingerIdentifier
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Keep
data class ApiPostPerson(@SerializedName("id") var patientId: String,
                        var projectId: String,
                        var userId: String,
                        var moduleId: String,
                        var fingerprints: HashMap<FingerIdentifier, ArrayList<ApiFingerprint>>) : PostGsonProcessable {

    @SkipSerialisationProperty
    val fingerprintsAsList
        get() = ArrayList(fingerprints.flatMap { t -> t.value })

    override fun gsonPostProcess() {
        //The server returns a map with Finerprint (quality and template), we need to set the fingerId
        fingerprints.mapValues { entry -> entry.value.forEach { it.fingerId = entry.key } }
    }
}

fun Person.toPostPerson(): ApiPostPerson =
    ApiPostPerson(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = HashMap(fingerprints
            .map(Fingerprint::toApiFingerprint)
            .groupBy { it.fingerId }
            .mapValues { ArrayList(it.value) })
    )

fun ApiPostPerson.toDomainPerson(): Person =
    Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        fingerprints = fingerprints.flatMap { (_, fingerFingerprints) ->
            fingerFingerprints.map(ApiFingerprint::toDomainFingerprint)
        },
        toSync = false
    )
