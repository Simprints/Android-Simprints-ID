package com.simprints.infra.config.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.domain.models.ProjectConfiguration

@Keep
internal data class ApiProjectConfiguration(
    val projectId: String,
    val general: ApiGeneralConfiguration,
    val face: ApiFaceConfiguration?,
    val fingerprint: ApiFingerprintConfiguration?,
    val consent: ApiConsentConfiguration,
    val identification: ApiIdentificationConfiguration,
    val synchronization: ApiSynchronizationConfiguration,
) {
    fun toDomain(): ProjectConfiguration =
        ProjectConfiguration(
            projectId,
            general.toDomain(),
            face?.toDomain(),
            fingerprint?.toDomain(),
            consent.toDomain(),
            identification.toDomain(),
            synchronization.toDomain(),
        )
}
