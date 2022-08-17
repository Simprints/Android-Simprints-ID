package com.simprints.id.data.db.subject.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.realm.models.DbFingerprintSample
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat

fun DbFingerprintSample.fromDbToDomain(): FingerprintSample =
    FingerprintSample(
        fingerIdentifier = IFingerIdentifier.values()[fingerIdentifier],
        template = template,
        templateQualityScore = templateQualityScore,
        format = IFingerprintTemplateFormat.valueOf(format)
    )

fun FingerprintSample.fromDomainToDb(): DbFingerprintSample =
    DbFingerprintSample(
        id,
        fingerIdentifier.ordinal,
        template,
        templateQualityScore,
        format.name
    )
