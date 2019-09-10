package com.simprints.id.data.db.person.local

import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.domain.Fingerprint
import com.simprints.id.tools.extensions.toRealmList
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class DbPerson(
    @PrimaryKey
    @Required
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
) : RealmObject()

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
