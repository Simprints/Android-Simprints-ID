package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.modality.Modality
import com.simprints.core.domain.sample.Sample
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample as RealmFaceSample

internal fun RealmFaceSample.toDomain(): Sample = Sample(
    id = id,
    template = template,
    format = format,
    referenceId = referenceId,
    modality = Modality.FACE,
)

internal fun Sample.toRealmFaceDb(): RealmFaceSample = RealmFaceSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.template = template
    sample.format = format
}
