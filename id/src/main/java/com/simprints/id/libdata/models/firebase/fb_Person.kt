package com.simprints.id.libdata.models.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.database.Exclude
import com.google.firebase.database.Query
import com.google.firebase.firestore.FieldValue
import com.google.gson.annotations.SerializedName
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Routes.patientsNode
import com.simprints.id.libdata.tools.Routes.projectRef
import com.simprints.id.libdata.tools.Utils
import com.simprints.libcommon.Person
import com.simprints.libsimprints.FingerIdentifier
import java.util.*

data class fb_Person(@SerializedName("id") var patientId: String = "",
                     var userId: String = "",
                     var moduleId: String = "",
                     var androidId: String = "",
                     var createdAt: Date = Utils.now(),
                     var updatedAt: Date = Utils.now(),
                     var fingerprints: HashMap<FingerIdentifier, ArrayList<fb_Fingerprint>> = hashMapOf()) {

    constructor (person: Person,
                 userId: String,
                 androidId: String,
                 moduleId: String) : this (
        patientId = person.guid,
        userId = userId,
        androidId = androidId,
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

    companion object {

        @Exclude
        fun getByUser(app: FirebaseApp, apiKey: String, userId: String): Query {
            return projectRef(app, apiKey).child(patientsNode()).orderByChild("userId").equalTo(userId)
        }

        @Exclude
        fun getByModuleId(app: FirebaseApp, apiKey: String, moduleId: String): Query {
            return projectRef(app, apiKey).child(patientsNode()).orderByChild("moduleId").equalTo(moduleId)
        }
    }
}
