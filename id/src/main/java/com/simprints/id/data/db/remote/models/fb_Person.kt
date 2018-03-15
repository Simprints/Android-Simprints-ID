package com.simprints.id.data.db.remote.models

import com.google.firebase.database.Exclude
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import com.simprints.id.data.db.local.models.rl_Person
import com.simprints.id.data.db.remote.tools.Utils
import com.simprints.libcommon.Person
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

data class fb_Person(@SerializedName("id") var patientId: String = "",
                     var userId: String = "",
                     var moduleId: String = "",
                     @ServerTimestamp var createdAt: Date = Utils.now(),
                     @ServerTimestamp var updatedAt: Date = Utils.now(),
                     private var fingerprints: HashMap<FingerIdentifier, ArrayList<fb_Fingerprint>> = hashMapOf()) {

    constructor (person: Person,
                 userId: String,
                 moduleId: String) : this (
        patientId = person.guid,
        userId = userId,
        moduleId = moduleId) {

        person.fingerprints
            .map { fb_Fingerprint(it) }
            .forEach {
                var listOfFingerprints = fingerprints[it.fingerId]
                if (listOfFingerprints == null) {
                    listOfFingerprints = arrayListOf()
                }

                listOfFingerprints.add(it)
            }
    }

    constructor (realmPerson: rl_Person) : this (
        patientId = realmPerson.patientId,
        userId = realmPerson.userId,
        moduleId = realmPerson.moduleId,
        createdAt = realmPerson.createdAt) {

        realmPerson.libPerson.fingerprints
            .map { fb_Fingerprint(it) }
            .forEach {
                var listOfFingerprints = fingerprints[it.fingerId]
                if (listOfFingerprints == null) {
                    listOfFingerprints = arrayListOf()
                }

                listOfFingerprints.add(it)
            }
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "patientId" to patientId,
            "userId" to userId,
            "moduleId" to moduleId,
            "createdAt" to createdAt,
            "syncTime" to FieldValue.serverTimestamp(),
            "fingerprints" to fingerprints)
    }

    fun getAllFingerprints(): ArrayList<fb_Fingerprint> {
        return ArrayList(fingerprints.flatMap { t -> t.value })
    }
}
