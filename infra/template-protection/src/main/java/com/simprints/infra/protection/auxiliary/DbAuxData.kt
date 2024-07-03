package com.simprints.infra.protection.auxiliary

import com.simprints.infra.realm.models.DbAuxData
import io.realm.kotlin.ext.toRealmList
import kotlin.also
import kotlin.collections.toIntArray

internal fun DbAuxData.fromDbToDomain(): TemplateAuxData = TemplateAuxData(
    subjectId = subjectId,
    exponents = exponents.toIntArray(),
    coefficients = coefficients.toIntArray(),
)

internal fun TemplateAuxData.fromDomainToDb(): DbAuxData = DbAuxData().also { auxData ->
    auxData.subjectId = subjectId
    auxData.exponents = exponents.toList().toRealmList()
    auxData.coefficients = coefficients.toList().toRealmList()
}
