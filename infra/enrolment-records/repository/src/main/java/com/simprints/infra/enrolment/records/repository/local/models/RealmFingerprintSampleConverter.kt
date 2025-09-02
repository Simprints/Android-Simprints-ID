package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample as RealmFingerprintSample

internal fun RealmFingerprintSample.toDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = DbSampleIdentifier.fromId(fingerIdentifier).toDomain(),
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FingerprintSample.toRealmDb(): RealmFingerprintSample = RealmFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = fingerIdentifier.fromDomain()?.id ?: -1
    sample.template = template
    sample.format = format
}
