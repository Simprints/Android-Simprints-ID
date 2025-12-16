package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample as RealmFaceSample

internal fun List<RealmFaceSample>.toDomain(): List<BiometricReference> = groupBy { it.referenceId }
    .map { (referenceId, templates) ->
        BiometricReference(
            referenceId = referenceId,
            format = templates.first().format,
            modality = Modality.FACE,
            templates = templates.map {
                BiometricTemplate(
                    id = it.id,
                    template = it.template,
                )
            },
        )
    }

internal fun BiometricReference.toRealmFaceDb(): List<RealmFaceSample> = templates.map {
    RealmFaceSample().also { sample ->
        sample.id = it.id
        sample.referenceId = referenceId
        sample.template = it.template
        sample.format = format
    }
}
