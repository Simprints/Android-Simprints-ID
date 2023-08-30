package com.simprints.infra.enrolment.records.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerIdentifier

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = IFingerIdentifier.values()[fingerIdentifier],
        template = template,
        templateQualityScore = templateQualityScore,
        format = format
    )

internal fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        format
    )
