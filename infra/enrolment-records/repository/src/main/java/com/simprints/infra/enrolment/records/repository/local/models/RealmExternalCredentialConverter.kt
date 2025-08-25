package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.infra.enrolment.records.realm.store.models.DbExternalCredential as RealmExternalCredential

internal fun ExternalCredential.toRealmDb(): RealmExternalCredential = RealmExternalCredential().also { sample ->
    sample.value = value.value
    sample.subjectId = subjectId
    sample.type = type.toString()
}
