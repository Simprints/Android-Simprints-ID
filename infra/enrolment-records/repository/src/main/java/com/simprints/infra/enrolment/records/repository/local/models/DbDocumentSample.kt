package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.document.DocumentSample
import com.simprints.infra.enrolment.records.realm.store.models.DbDocumentSample

internal fun DbDocumentSample.fromDbToDomain(): DocumentSample = DocumentSample(
    id = id,
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun DocumentSample.fromDomainToDb(): DbDocumentSample = DbDocumentSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.template = template
    sample.format = format
}
