package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.sample.Sample
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate

internal fun DbBiometricTemplate.toSample(): Sample = Sample(
    id = uuid,
    identifier = DbSampleIdentifier.fromId(identifier).toDomain(),
    template = templateData,
    format = format,
    referenceId = referenceId,
    modality = DbModality.fromId(modality).toDomain(),
)

internal fun Sample.toRoomDb(subjectId: String) = DbBiometricTemplate(
    uuid = id,
    identifier = identifier.fromDomain().id,
    templateData = template,
    format = format,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = modality.fromDomain().id,
)
