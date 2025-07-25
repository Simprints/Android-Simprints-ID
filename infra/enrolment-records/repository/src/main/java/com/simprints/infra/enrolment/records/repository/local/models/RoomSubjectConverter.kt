package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.room.store.models.Modality
import com.simprints.infra.enrolment.records.room.store.models.SubjectBiometrics
import java.util.Date

internal fun SubjectBiometrics.toDomain() = Subject(
    subjectId = subject.subjectId.toString(),
    projectId = subject.projectId,
    attendantId = subject.attendantId.asTokenizableEncrypted(),
    moduleId = subject.moduleId.asTokenizableEncrypted(),
    createdAt = subject.createdAt?.toDate(),
    updatedAt = subject.updatedAt?.toDate(),
    fingerprintSamples = biometricTemplates.filter { it.modality == Modality.FINGERPRINT.id }.map { it.toFingerprintSample() },
    faceSamples = biometricTemplates.filter { it.modality == Modality.FACE.id }.map { it.toFaceSample() },
)

fun Long.toDate() = Date(this)
