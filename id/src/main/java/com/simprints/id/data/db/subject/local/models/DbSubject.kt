package com.simprints.eventsystem.subject.local.models

import com.simprints.eventsystem.subject.domain.FaceSample
import com.simprints.eventsystem.subject.domain.FingerprintSample
import com.simprints.eventsystem.subject.domain.Subject
import com.simprints.id.tools.extensions.toRealmList
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

open class DbSubject(
    @PrimaryKey
    @Required
    var subjectId: String = "",

    @Required
    var projectId: String = "",

    @Required
    var attendantId: String = "",

    @Required
    var moduleId: String = "",

    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    @Deprecated("See SubjectToEventDbMigrationManagerImpl doc")
    var toSync: Boolean = false,

    var fingerprintSamples: RealmList<DbFingerprintSample> = RealmList(),

    var faceSamples: RealmList<DbFaceSample> = RealmList()

) : RealmObject()

fun DbSubject.fromDbToDomain(): Subject =
    Subject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
        faceSamples = faceSamples.map(DbFaceSample::fromDbToDomain)
    )

fun Subject.fromDomainToDb(): DbSubject =
    DbSubject(
        subjectId = subjectId,
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(FingerprintSample::fromDomainToDb).toRealmList(),
        faceSamples = faceSamples.map(FaceSample::fromDomainToDb).toRealmList()
    )
