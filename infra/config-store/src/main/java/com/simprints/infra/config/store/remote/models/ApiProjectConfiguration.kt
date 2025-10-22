package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.ProjectConfiguration

@Keep
internal data class ApiProjectConfiguration(
    val id: String,
    val projectId: String,
    val updatedAt: String,
    val general: ApiGeneralConfiguration,
    val face: ApiFaceConfiguration?,
    val fingerprint: ApiFingerprintConfiguration?,
    val consent: ApiConsentConfiguration,
    val identification: ApiIdentificationConfiguration,
    val synchronization: ApiSynchronizationConfiguration,
    val multiFactorId: ApiMultiFactorIdConfiguration?,
    val custom: Map<String, Any>?,
) {
    fun toDomain(): ProjectConfiguration = ProjectConfiguration(
        id = id,
        projectId = projectId,
        updatedAt = updatedAt,
        general = general.toDomain(),
        face = face?.toDomain(),
        fingerprint = fingerprint?.toDomain(),
        consent = consent.toDomain(),
        identification = identification.toDomain(),
        synchronization = synchronization.toDomain(),
        multifactorId = multiFactorId?.toDomain(),
        custom = custom,
    )
}
