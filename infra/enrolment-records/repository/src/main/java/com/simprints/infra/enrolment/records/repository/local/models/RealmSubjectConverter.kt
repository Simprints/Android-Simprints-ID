package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.sample.Sample
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.isTokenized
import com.simprints.infra.enrolment.records.realm.store.models.DbExternalCredential
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.toDate
import com.simprints.infra.enrolment.records.realm.store.models.toRealmInstant
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.types.RealmUUID
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject as RealmSubject

internal fun RealmSubject.toDomain(): Subject {
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
        samples = fingerprintSamples.map(DbFingerprintSample::toDomain) +
            faceSamples.map(DbFaceSample::toDomain),
        externalCredentials = externalCredentials.map(DbExternalCredential::toDomain),
    )
}

internal fun Subject.toRealmDb(): RealmSubject = RealmSubject().also { subject ->
    subject.subjectId = RealmUUID.from(subjectId)
    subject.projectId = projectId
    subject.attendantId = attendantId.value
    subject.moduleId = moduleId.value
    subject.createdAt = createdAt?.toRealmInstant()
    subject.updatedAt = updatedAt?.toRealmInstant()
    subject.isModuleIdTokenized = moduleId.isTokenized()
    subject.isAttendantIdTokenized = attendantId.isTokenized()
    subject.externalCredentials = externalCredentials.map(ExternalCredential::toRealmDb).toRealmList()

    subject.fingerprintSamples = samples
        .filter { it.modality == Modality.FINGERPRINT }
        .map(Sample::toRealmFingerprintDb)
        .toRealmList()
    subject.faceSamples = samples
        .filter { it.modality == Modality.FACE }
        .map(Sample::toRealmFaceDb)
        .toRealmList()
}
