package com.simprints.id.data.db.subject.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.tools.extensions.toRealmList
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.infra.realm.models.DbSubject
import java.util.*

fun DbSubject.fromDbToDomain(): Subject =
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

fun Subject.fromDomainToDb(): DbSubject =
    DbSubject(
        subjectId = UUID.fromString(subjectId),
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(FingerprintSample::fromDomainToDb)
            .toRealmList(),
        faceSamples = faceSamples.map(FaceSample::fromDomainToDb).toRealmList()
    )
