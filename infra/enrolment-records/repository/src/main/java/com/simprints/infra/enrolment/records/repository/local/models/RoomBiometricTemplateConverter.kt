package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbModality
import com.simprints.infra.enrolment.records.room.store.models.fromDomain
import com.simprints.infra.enrolment.records.room.store.models.toDomain

// Biometric reference data is stored as part of the biometric template data, so we group them back in the domain model
internal fun List<DbBiometricTemplate>.toBiometricReferences(): List<BiometricReference> = groupBy { it.referenceId }
    .filter { (_, templates) -> templates.isNotEmpty() }
    .map { (referenceId, templates) ->
        val firstTemplate = templates.first()

        BiometricReference(
            referenceId = referenceId,
            format = firstTemplate.format,
            modality = DbModality.fromId(firstTemplate.modality).toDomain(),
            templates = templates.map {
                BiometricTemplate(
                    id = it.uuid,
                    template = it.templateData,
                    identifier = DbTemplateIdentifier.fromId(it.identifier).toDomain(),
                )
            },
        )
    }

internal fun BiometricReference.toRoomDbTemplate(subjectId: String): List<DbBiometricTemplate> = templates.map {
    DbBiometricTemplate(
        uuid = it.id,
        templateData = it.template,
        format = format,
        identifier = it.identifier.fromDomain()?.id,
        subjectId = subjectId,
        referenceId = referenceId,
        modality = modality.fromDomain().id,
    )
}
