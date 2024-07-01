package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.infra.enrolment.records.store.domain.models.TemplateAuxData
import com.simprints.infra.realm.models.DbAuxData
import io.realm.kotlin.ext.toRealmList

internal fun DbAuxData.fromDbToDomain(): TemplateAuxData = TemplateAuxData(
    subjectId = subjectId,
    exponents = exponents.toIntArray(),
    coefficients = coefficients.toIntArray(),
)

internal fun TemplateAuxData.fromDomainToDb(): DbAuxData =
    DbAuxData().also { auxData ->
        auxData.subjectId = subjectId
        auxData.exponents = exponents.toList().toRealmList()
        auxData.coefficients = coefficients.toList().toRealmList()
    }
