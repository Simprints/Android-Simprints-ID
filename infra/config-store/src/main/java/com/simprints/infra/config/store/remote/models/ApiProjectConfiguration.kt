package com.simprints.infra.config.store.remote.models

import androidx.annotation.Keep
import com.simprints.core.tools.json.AnyPrimitiveSerializer
import com.simprints.infra.config.store.models.ProjectConfiguration
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiProjectConfiguration(
    val id: String,
    val projectId: String,
    val updatedAt: String,
    val general: ApiGeneralConfiguration,
    val face: ApiFaceConfiguration? = null,
    val fingerprint: ApiFingerprintConfiguration? = null,
    val consent: ApiConsentConfiguration,
    val identification: ApiIdentificationConfiguration,
    val synchronization: ApiSynchronizationConfiguration,
    val multiFactorId: ApiMultiFactorIdConfiguration? = null,
    val custom: Map<String, @Serializable(with = AnyPrimitiveSerializer::class) Any>? = null,
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
