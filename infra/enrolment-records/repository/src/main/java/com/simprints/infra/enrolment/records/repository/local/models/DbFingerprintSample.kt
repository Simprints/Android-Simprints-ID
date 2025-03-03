package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = IFingerIdentifier.entries[fingerIdentifier],
    template = template,
    templateQualityScore = templateQualityScore,
    format = format,
    referenceId = referenceId,
)

internal fun FingerprintSample.fromDomainToDb(): DbFingerprintSample = DbFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = fingerIdentifier.ordinal
    sample.template = template
    sample.templateQualityScore = templateQualityScore
    sample.format = format
}
