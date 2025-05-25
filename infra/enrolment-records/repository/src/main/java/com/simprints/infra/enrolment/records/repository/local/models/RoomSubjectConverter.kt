package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.Modality
import java.util.Date

internal fun List<DbBiometricTemplate>.toDomain(): Subject {
    val firstTemplate = first()
    return Subject(
        subjectId = firstTemplate.subjectId,
        projectId = firstTemplate.projectId,
        attendantId = firstTemplate.attendantId.asTokenizableEncrypted(),
        moduleId = firstTemplate.moduleId.asTokenizableEncrypted(),
        createdAt = firstTemplate.createdAt?.toDate(),
        updatedAt = firstTemplate.updatedAt?.toDate(),
        fingerprintSamples = filter { it.modality == Modality.FINGERPRINT.id }.map { it.toFingerprintSample() },
        faceSamples = filter { it.modality == Modality.FACE.id }.map { it.toFaceSample() },
    )
}

fun Long.toDate() = Date(this)
