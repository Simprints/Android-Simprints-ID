package com.simprints.id.libdata.models.firebase

import com.google.firebase.FirebaseApp
import com.google.firebase.database.Exclude
import com.google.firebase.database.Query
import com.google.firebase.firestore.FieldValue
import com.simprints.id.libdata.models.realm.rl_Person
import com.simprints.id.libdata.tools.Routes.patientsNode
import com.simprints.id.libdata.tools.Routes.projectRef
import com.simprints.id.libdata.tools.Utils
import com.simprints.libcommon.Person
import java.util.*

data class fb_Person( var patientId: String,
                      var userId: String = "",
                      var androidId: String = "",
                      var moduleId: String = "",
                      var createdAt: Date = Date(0),
                      var syncTime: Date = Date(0)) {

    lateinit var fingerprints: MutableList<fb_Fingerprint>

    constructor (person: Person,
                 userId: String,
                 androidId: String,
                 moduleId: String) : this (
        patientId = person.guid,
        userId = userId,
        androidId = androidId,
        moduleId = moduleId,
        createdAt = Utils.now()) {

        this.fingerprints = ArrayList()
        person.fingerprints
            .map { fb_Fingerprint(it) }
            .forEach { fingerprints.add(it) }
    }

    constructor (realmPerson: rl_Person) : this (
        patientId = realmPerson.patientId,
        userId = realmPerson.userId,
        androidId = realmPerson.androidId,
        moduleId = realmPerson.moduleId,
        createdAt = realmPerson.createdAt) {

        this.fingerprints = ArrayList()
        realmPerson.libPerson.fingerprints
            .map { fb_Fingerprint(it) }
            .forEach { fingerprints.add(it) }
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "patientId" to patientId,
            "userId" to userId,
            "moduleId" to moduleId,
            "androidId" to androidId,
            "createdAt" to createdAt,
            "syncTime" to FieldValue.serverTimestamp(),
            "fingerprints" to fingerprints)
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
