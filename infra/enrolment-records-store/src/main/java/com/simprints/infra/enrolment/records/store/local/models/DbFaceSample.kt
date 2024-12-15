package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.realm.models.DbFaceSample

internal fun DbFaceSample.fromDbToDomain(): FaceSample = FaceSample(
    id = id,
    template = template,
    format = format,
)

internal fun FaceSample.fromDomainToDb(): DbFaceSample = DbFaceSample().also { sample ->
    sample.id = id
    sample.template = template
    sample.format = format
}
