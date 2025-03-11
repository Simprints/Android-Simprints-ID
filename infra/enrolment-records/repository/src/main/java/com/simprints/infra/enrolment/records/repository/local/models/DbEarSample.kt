package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.ear.EarSample
import com.simprints.infra.enrolment.records.realm.store.models.DbEarSample

internal fun DbEarSample.fromDbToDomain(): EarSample = EarSample(
    id = id,
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun EarSample.fromDomainToDb(): DbEarSample = DbEarSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.template = template
    sample.format = format
}
