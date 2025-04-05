package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample as RealmFingerprintSample

internal fun RealmFingerprintSample.toDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = IFingerIdentifier.entries[fingerIdentifier],
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FingerprintSample.toRealmDb(): RealmFingerprintSample = RealmFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = fingerIdentifier.ordinal
    sample.template = template
    sample.format = format
}
