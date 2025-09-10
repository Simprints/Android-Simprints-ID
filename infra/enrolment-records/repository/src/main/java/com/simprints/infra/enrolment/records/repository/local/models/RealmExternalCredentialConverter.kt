package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.realm.store.models.DbExternalCredential as RealmExternalCredential

internal fun ExternalCredential.toRealmDb(): RealmExternalCredential = RealmExternalCredential().also { sample ->
    sample.id = id
    sample.value = value.value
    sample.subjectId = subjectId
    sample.type = type.toString()
}

internal fun RealmExternalCredential.toDomain(): ExternalCredential = ExternalCredential(
    id = this.id,
    value = this.value.asTokenizableEncrypted(),
    subjectId = this.subjectId,
    type = ExternalCredentialType.valueOf(this.type)
)
