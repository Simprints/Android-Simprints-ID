package com.simprints.id.data.db.local.realm.models

import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.data.db.remote.models.toDomainFingerprint
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.id.tools.extensions.toRealmList
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class DbPerson(
    @PrimaryKey
    var patientId: String = "",

    @Required
    var projectId: String = "",

    @Required
    var userId: String = "",

    @Required
    var moduleId: String = "",

    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    var toSync: Boolean = false,

    @Required
    var fingerprints: RealmList<DbFingerprint> = RealmList()
) : RealmObject() {

    companion object {
        const val USER_ID_FIELD = "userId"
        const val PROJECT_ID_FIELD = "projectId"
        const val PATIENT_ID_FIELD = "patientId"
        const val MODULE_ID_FIELD = "moduleId"
        const val TO_SYNC_FIELD = "toSync"
        const val UPDATE_TIME_FIELD = "updatedAt"
        const val CREATE_TIME_FIELD = "createdAt"
    }

    constructor(person: fb_Person, toSync: Boolean = person.updatedAt == null || person.createdAt == null):
     this(person.patientId, person.projectId, person.userId, person.moduleId, person.createdAt, person.updatedAt, toSync,
         person.fingerprintsAsList
             .map { it.toDomainFingerprint().toRealmFingerprint() }
             .filter { it.template != null}
             .toRealmList()
         )

}

fun DbPerson.toDomainPerson(): Person =
    Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprints = fingerprints.map(DbFingerprint::toDomainFingerprint)
    )

fun Person.toRealmPerson(): DbPerson =
    DbPerson(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprints = fingerprints.map(Fingerprint::toRealmFingerprint).toRealmList()
    )
