package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.sample.Sample
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbModality
import com.simprints.infra.enrolment.records.room.store.models.fromDomain
import com.simprints.infra.enrolment.records.room.store.models.toDomain

internal fun DbBiometricTemplate.toSample(): Sample = Sample(
    id = uuid,
    identifier = identifier?.let { DbSampleIdentifier.fromId(it) }.toDomain(),
    modality = DbModality.fromId(modality).toDomain(),
    referenceId = referenceId,
    format = format,
    template = templateData,
)

internal fun Sample.toRoomDb(subjectId: String): DbBiometricTemplate = DbBiometricTemplate(
    uuid = id,
    templateData = template,
    format = format,
    identifier = identifier.fromDomain()?.id,
    subjectId = subjectId,
    referenceId = referenceId,
    modality = modality.fromDomain().id,
)
