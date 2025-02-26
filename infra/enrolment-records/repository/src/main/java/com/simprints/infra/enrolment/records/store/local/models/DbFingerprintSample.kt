package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.realm.models.DbFingerprintSample

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = IFingerIdentifier.entries[fingerIdentifier],
    template = template,
    templateQualityScore = templateQualityScore,
    format = format,
)

internal fun FingerprintSample.fromDomainToDb(): DbFingerprintSample = DbFingerprintSample().also { sample ->
    sample.id = id
    sample.fingerIdentifier = fingerIdentifier.ordinal
    sample.template = template
    sample.templateQualityScore = templateQualityScore
    sample.format = format
}
