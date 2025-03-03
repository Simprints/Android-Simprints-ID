package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample

internal fun DbFaceSample.fromDbToDomain(): FaceSample = FaceSample(
    id = id,
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FaceSample.fromDomainToDb(): DbFaceSample = DbFaceSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.template = template
    sample.format = format
}
