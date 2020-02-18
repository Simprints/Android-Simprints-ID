package com.simprints.id.data.db.person.local.models

import com.simprints.id.data.db.person.domain.FaceSample
import com.simprints.id.data.db.person.domain.FingerprintSample
import com.simprints.id.data.db.person.domain.Person
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

    var fingerprintSamples: RealmList<DbFingerprintSample> = RealmList(),

    var faceSamples: RealmList<DbFaceSample> = RealmList()

) : RealmObject()

fun DbPerson.fromDbToDomain(): Person =
    Person(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
        faceSamples = faceSamples.map(DbFaceSample::fromDbToDomain)
    )

fun Person.fromDomainToDb(): DbPerson =
    DbPerson(
        patientId = patientId,
        projectId = projectId,
        userId = userId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(FingerprintSample::fromDomainToDb).toRealmList(),
        faceSamples = faceSamples.map(FaceSample::fromDomainToDb).toRealmList()
    )
