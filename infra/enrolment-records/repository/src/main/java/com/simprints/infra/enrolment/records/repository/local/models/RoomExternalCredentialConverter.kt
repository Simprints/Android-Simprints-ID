package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential

internal fun DbExternalCredential.toDomain(): ExternalCredential = ExternalCredential(
    id = id,
    value = value.asTokenizableEncrypted(),
    subjectId = subjectId,
    type = ExternalCredentialType.valueOf(type),
)

internal fun ExternalCredential.toRoomDbCredentials(): DbExternalCredential = DbExternalCredential(
    id = id,
    value = value.value,
    subjectId = subjectId,
    type = type.name,
)
