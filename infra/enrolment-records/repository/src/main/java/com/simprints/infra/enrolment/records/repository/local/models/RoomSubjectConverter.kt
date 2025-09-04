package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.room.store.models.DbModality
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics
import java.util.Date

internal fun SubjectBiometrics.toDomain() = Subject(
    subjectId = subject.subjectId.toString(),
    projectId = subject.projectId,
    attendantId = subject.attendantId.asTokenizableEncrypted(),
    moduleId = subject.moduleId.asTokenizableEncrypted(),
    createdAt = subject.createdAt?.toDate(),
    updatedAt = subject.updatedAt?.toDate(),
    fingerprintSamples = biometricTemplates.filter { it.modality == DbModality.FINGERPRINT.id }.map { it.toSample() },
    faceSamples = biometricTemplates.filter { it.modality == DbModality.FACE.id }.map { it.toSample() },
    externalCredentials = externalCredentials.map { it.toDomain() },
)

fun Long.toDate() = Date(this)
