package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.store.domain.models.Subject
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.infra.realm.models.DbSubject
import com.simprints.infra.realm.models.toDate
import com.simprints.infra.realm.models.toRealmInstant
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmUUID

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
        createdAt = createdAt?.toDate(),
        updatedAt = updatedAt?.toDate(),
        toSync = toSync,
        fingerprintSamples = fingerprintSamples.map(DbFingerprintSample::fromDbToDomain),
        faceSamples = faceSamples.map(DbFaceSample::fromDbToDomain),
    )
}

internal fun Subject.fromDomainToDb(): DbSubject = DbSubject().also { subject ->
    subject.subjectId = RealmUUID.from(subjectId)
    subject.projectId = projectId
    subject.attendantId = attendantId.value
    subject.moduleId = moduleId.value
    subject.createdAt = createdAt?.toRealmInstant()
    subject.updatedAt = updatedAt?.toRealmInstant()
    subject.toSync = toSync
    subject.fingerprintSamples =
        fingerprintSamples.map(FingerprintSample::fromDomainToDb).toRealmList()
    subject.faceSamples = faceSamples.map(FaceSample::fromDomainToDb).toRealmList()
    subject.isModuleIdTokenized = moduleId.isTokenized()
    subject.isAttendantIdTokenized = attendantId.isTokenized()
}
