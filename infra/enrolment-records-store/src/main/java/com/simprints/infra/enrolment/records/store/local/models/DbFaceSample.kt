package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.realm.models.DbFaceSample

internal fun DbFaceSample.fromDbToDomain(): FaceSample = FaceSample(
    id = uuid,
    template = template,
    format = format,
)

internal fun FaceSample.fromDomainToDb(subjectId: String): DbFaceSample = DbFaceSample(subjectId = subjectId).also { sample ->
    sample.uuid = id
    sample.template = template
    sample.format = format
}
