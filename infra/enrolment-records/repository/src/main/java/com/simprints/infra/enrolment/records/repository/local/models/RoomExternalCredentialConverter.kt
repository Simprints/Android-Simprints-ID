package com.simprints.infra.enrolment.records.repository.local.models

import com.simprints.core.domain.externalcredential.ExternalCredential
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.room.store.models.DbBiometricTemplate
import com.simprints.infra.enrolment.records.room.store.models.DbExternalCredential
import com.simprints.infra.enrolment.records.room.store.models.Modality

internal fun DbExternalCredential.toDomain(): ExternalCredential = ExternalCredential(
    value = value,
    subjectId = subjectId,
    type = ExternalCredentialType.valueOf(type)
)

internal fun ExternalCredential.toRoomDb(): DbExternalCredential = DbExternalCredential(
    value = value,
    subjectId = subjectId,
    type = type.name
)

