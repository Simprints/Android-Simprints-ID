package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.serialization.SimJson

internal fun ProjectConfiguration.toProto(): ProtoProjectConfiguration = ProtoProjectConfiguration
    .newBuilder()
    .setId(id)
    .setProjectId(projectId)
    .setUpdatedAt(updatedAt)
    .setConsent(consent.toProto())
    .setGeneral(general.toProto())
    .setIdentification(identification.toProto())
    .setSynchronization(synchronization.toProto())
    .also {
        if (face != null) it.face = face.toProto()
        if (fingerprint != null) it.fingerprint = fingerprint.toProto()
        if (multifactorId != null) it.multiFactorId = multifactorId.toProto()
    }.also {
        if (custom != null) {
            try {
                val customJson = SimJson.encodeToString(custom)
                it.setCustomJson(customJson)
            } catch (_: Exception) {
                // It is safer to not have custom config, than broken one
                it.clearCustomJson()
            }
        }
    }.build()

internal fun ProtoProjectConfiguration.toDomain(): ProjectConfiguration = ProjectConfiguration(
    id = id,
    projectId = projectId,
    updatedAt = updatedAt,
    general = general.toDomain(),
    face = hasFace().let { if (it) face.toDomain() else null },
    fingerprint = hasFingerprint().let { if (it) fingerprint.toDomain() else null },
    consent = consent.toDomain(),
    identification = identification.toDomain(),
    synchronization = synchronization.toDomain(),
    multifactorId = multiFactorId?.toDomain(),
    custom = customJson?.takeIf { it.isNotBlank() }?.let {
        try {
            SimJson.decodeFromString(it)
        } catch (e: Exception) {
            // It is safer to not have custom config, than broken one
            null
        }
    },
)
