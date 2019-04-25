package com.simprints.id.data.db.local.realm.models

import com.simprints.id.data.db.remote.models.ApiPerson
import com.simprints.id.data.db.remote.models.toDomainFingerprint
import com.simprints.id.domain.fingerprint.Fingerprint
import com.simprints.id.domain.Person
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

    constructor(person: ApiPerson, toSync: Boolean = person.updatedAt == null || person.createdAt == null):
     this(person.patientId, person.projectId, person.userId, person.moduleId, person.createdAt, person.updatedAt, toSync,
         person.fingerprints.map { it.toDomainFingerprint().toRealmFingerprint() }.toRealmList()
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
