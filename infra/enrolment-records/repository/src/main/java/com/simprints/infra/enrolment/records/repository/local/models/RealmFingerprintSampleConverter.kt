package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample as RealmFingerprintSample

internal fun List<RealmFingerprintSample>.toDomain(): List<BiometricReference> = groupBy { it.referenceId }
    .map { (referenceId, templates) ->
        BiometricReference(
            referenceId = referenceId,
            format = templates.first().format,
            modality = Modality.FINGERPRINT,
            templates = templates.map {
                BiometricTemplate(
                    id = it.id,
                    template = it.template,
                    identifier = DbTemplateIdentifier.fromId(it.fingerIdentifier).toDomain(),
                )
            },
        )
    }

internal fun BiometricReference.toRealmFingerprintDb(): List<RealmFingerprintSample> = templates.map {
    RealmFingerprintSample().also { sample ->
        sample.id = it.id
        sample.referenceId = referenceId
        sample.fingerIdentifier = it.identifier.fromDomain()?.id ?: -1
        sample.template = it.template
        sample.format = format
    }
}
