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
    val custom: Map<String, Any>?,
) {

    fun toDomain(): ProjectConfiguration = ProjectConfiguration(
        id,
        projectId,
        updatedAt,
        general.toDomain(),
        face?.toDomain(),
        fingerprint?.toDomain(),
        consent.toDomain(),
        identification.toDomain(),
        synchronization.toDomain(),
        custom,
    )
}
