package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.Modality

internal fun DbBiometricTemplate.toFingerprintSample(): FingerprintSample = FingerprintSample(
    id = uuid,
    fingerIdentifier = IFingerIdentifier.fromId(identifier!!).toDomain(),
    template = templateData,
    format = format,
    referenceId = referenceId,
)

internal fun DbBiometricTemplate.toFaceSample(): FaceSample = FaceSample(
    id = uuid,
    template = templateData,
    format = format,
    referenceId = referenceId,
)

internal fun FaceSample.toRoomDb(
    subjectId: String,
    projectId: String,
    attendantId: String,
    moduleId: String,
    createdAt: Long?,
    updatedAt: Long?,
): DbBiometricTemplate = DbBiometricTemplate(
    uuid = id,
    templateData = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = Modality.FACE.id,
    projectId = projectId,
    attendantId = attendantId,
    moduleId = moduleId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun FingerprintSample.toRoomDb(
    subjectId: String,
    projectId: String,
    attendantId: String,
    moduleId: String,
    createdAt: Long?,
    updatedAt: Long?,
) = DbBiometricTemplate(
    uuid = id,
    identifier = fingerIdentifier.fromDomain().id,
    templateData = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = Modality.FINGERPRINT.id,
    projectId = projectId,
    attendantId = attendantId,
    moduleId = moduleId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
