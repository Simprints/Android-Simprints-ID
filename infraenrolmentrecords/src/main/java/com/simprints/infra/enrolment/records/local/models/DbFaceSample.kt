package com.simprints.infra.enrolment.records.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.realm.models.DbFaceSample

internal fun DbFaceSample.fromDbToDomain(): FaceSample =
    FaceSample(template = template, format = format)

internal fun FaceSample.fromDomainToDb(): DbFaceSample =
    DbFaceSample(
        id = id,
        template = template,
        format = format
    )
