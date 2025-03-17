package com.simprints.infra.enrolment.records.store.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.realm.models.DbFingerprintSample

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample = FingerprintSample(
    id = uuid,
    fingerIdentifier = IFingerIdentifier.values()[fingerIdentifier],
    template = template,
    templateQualityScore = templateQualityScore,
    format = format,
)

internal fun FingerprintSample.fromDomainToDb(subjectId: String): DbFingerprintSample =
    DbFingerprintSample(subjectId = subjectId).also { sample ->
        sample.uuid = id
        sample.fingerIdentifier = fingerIdentifier.ordinal
        sample.template = template
        sample.templateQualityScore = templateQualityScore
        sample.format = format
    }
