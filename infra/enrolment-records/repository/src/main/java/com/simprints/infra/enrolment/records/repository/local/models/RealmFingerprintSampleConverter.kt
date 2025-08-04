package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample as RealmFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbExternalCredential as RealmExternalCredential

internal fun RealmFingerprintSample.toDomain(): FingerprintSample = FingerprintSample(
    id = id,
    fingerIdentifier = IFingerIdentifier.fromId(fingerIdentifier).toDomain(),
    template = template,
    format = format,
    referenceId = referenceId,
)

internal fun FingerprintSample.toRealmDb(): RealmFingerprintSample = RealmFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = fingerIdentifier.fromDomain().id
    sample.template = template
    sample.format = format
}

internal fun ExternalCredential.toRealmDb(): RealmExternalCredential = RealmExternalCredential().also { sample ->
    sample.value = value.value
    sample.subjectId = subjectId
    sample.type = type.toString()
}
