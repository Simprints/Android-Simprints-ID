package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.realm.store.models.toDate
import com.simprints.infra.enrolment.records.realm.store.models.toRealmInstant
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
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
    subject.fingerprintSamples =
        fingerprintSamples.map(FingerprintSample::fromDomainToDb).toRealmList()
    subject.faceSamples = faceSamples.map(FaceSample::fromDomainToDb).toRealmList()
    subject.isModuleIdTokenized = moduleId.isTokenized()
    subject.isAttendantIdTokenized = attendantId.isTokenized()
}
