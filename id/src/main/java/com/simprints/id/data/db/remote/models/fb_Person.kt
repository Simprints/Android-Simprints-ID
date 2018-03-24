package com.simprints.id.data.db.remote.models

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.libcommon.Person
import com.simprints.libsimprints.FingerIdentifier
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class fb_Person(@SerializedName("id") var patientId: String = "",
                     var projectId: String = "",
                     var userId: String = "",
                     var moduleId: String = "",
                     @ServerTimestamp var createdAt: Date? = null,
                     @ServerTimestamp var updatedAt: Date? = null,
                     private var fingerprints: HashMap<FingerIdentifier, ArrayList<fb_Fingerprint>> = hashMapOf()) {

    constructor (person: Person,
                 projectId: String,
                 userId: String,
                 moduleId: String) : this (
        patientId = person.guid,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId) {

        fingerprints = HashMap(
            person.fingerprints
                .map { fb_Fingerprint(it) }
                .groupBy { it.fingerId }
                .mapValues { ArrayList(it.value) })
    }

    constructor (realmPerson: rl_Person) : this (
        patientId = realmPerson.patientId,
        projectId = realmPerson.projectId,
        userId = realmPerson.userId,
        moduleId = realmPerson.moduleId,
        createdAt = realmPerson.createdAt) {

        fingerprints = HashMap(
            realmPerson.libPerson.fingerprints
                .map { fb_Fingerprint(it) }
                .groupBy { it.fingerId }
                .mapValues { ArrayList(it.value) })
    }

    val fingerprintsAsList = ArrayList(fingerprints.flatMap { t -> t.value })
}
