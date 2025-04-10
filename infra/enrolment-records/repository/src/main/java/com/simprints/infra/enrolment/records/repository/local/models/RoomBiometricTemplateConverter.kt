package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.Modality

internal fun DbBiometricTemplate.toFingerprintSample(): FingerprintSample = FingerprintSample(
    id = uuid,
    fingerIdentifier = IFingerIdentifier.entries[identifier!!],
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

internal fun FaceSample.toRoomDb(subjectId: String): DbBiometricTemplate = DbBiometricTemplate(
    uuid = id,
    templateData = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = Modality.FACE.ordinal,
)

internal fun FingerprintSample.toRoomDb(subjectId: String) = DbBiometricTemplate(
    uuid = id,
    identifier = fingerIdentifier.ordinal,
    templateData = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = Modality.FINGERPRINT.ordinal,
)
