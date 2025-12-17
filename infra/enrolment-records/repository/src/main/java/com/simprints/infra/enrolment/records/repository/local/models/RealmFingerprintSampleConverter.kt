package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.sample.Sample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample as RealmFingerprintSample

internal fun RealmFingerprintSample.toDomain(): Sample = Sample(
    id = id,
    template = BiometricTemplate(
        identifier = DbTemplateIdentifier.fromId(fingerIdentifier).toDomain(),
        template = template,
    ),
    format = format,
    referenceId = referenceId,
    modality = Modality.FINGERPRINT,
)

internal fun Sample.toRealmFingerprintDb(): RealmFingerprintSample = RealmFingerprintSample().also { sample ->
    sample.id = id
    sample.referenceId = referenceId
    sample.fingerIdentifier = template.identifier.fromDomain()?.id ?: -1
    sample.template = template.template
    sample.format = format
}
