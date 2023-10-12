package com.simprints.infra.enrolment.records.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.infra.realm.models.DbSubject
import io.realm.RealmList
import java.util.UUID

internal fun DbSubject.fromDbToDomain(): Subject {
    val attendantId =
        if (isAttendantIdTokenized) attendantId.asTokenizableEncrypted() else attendantId.asTokenizableRaw()
    val moduleId =
        if (isModuleIdTokenized) moduleId.asTokenizableEncrypted() else moduleId.asTokenizableRaw()
    return Subject(
        subjectId = subjectId.toString(),
        projectId = projectId,
        attendantId = attendantId,
        moduleId = moduleId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
        faceSamples = faceSamples.map(DbFaceSample::fromDbToDomain),
    )
}

internal fun Subject.fromDomainToDb(): DbSubject {
    return DbSubject(
        subjectId = UUID.fromString(subjectId),
        projectId = projectId,
        attendantId = attendantId.value,
        moduleId = moduleId.value,
        createdAt = createdAt,
        updatedAt = updatedAt,
        toSync = toSync,
        fingerprintSamples = RealmList(
            *fingerprintSamples.map(FingerprintSample::fromDomainToDb).toTypedArray()
        ),
        faceSamples = RealmList(*faceSamples.map(FaceSample::fromDomainToDb).toTypedArray()),
        isModuleIdTokenized = moduleId.isTokenized(),
        isAttendantIdTokenized = attendantId.isTokenized(),
    )
}