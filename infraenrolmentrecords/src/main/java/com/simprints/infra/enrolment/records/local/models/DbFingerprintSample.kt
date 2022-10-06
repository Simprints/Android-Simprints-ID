package com.simprints.infra.enrolment.records.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

internal fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = IFingerIdentifier.values()[fingerIdentifier],
        template = template,
        templateQualityScore = templateQualityScore,
        format = IFingerprintTemplateFormat.valueOf(format)
    )

internal fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        format.name
    )
