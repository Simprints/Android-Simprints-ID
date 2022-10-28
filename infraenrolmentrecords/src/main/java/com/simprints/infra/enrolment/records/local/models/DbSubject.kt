package com.simprints.infra.enrolment.records.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.infra.realm.models.DbSubject
import io.realm.RealmList
import java.util.*

internal fun DbSubject.fromDbToDomain(): Subject =
    Subject(
        subjectId = subjectId.toString(),
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
        faceSamples = faceSamples.map(DbFaceSample::fromDbToDomain)
    )

internal fun Subject.fromDomainToDb(): DbSubject =
    DbSubject(
        subjectId = UUID.fromString(subjectId),
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = RealmList(
            *fingerprintSamples.map(FingerprintSample::fromDomainToDb).toTypedArray()
        ),
        faceSamples = RealmList(*faceSamples.map(FaceSample::fromDomainToDb).toTypedArray())
    )
