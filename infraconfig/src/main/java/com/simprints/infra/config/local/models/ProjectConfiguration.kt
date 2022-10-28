package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.ProjectConfiguration

internal fun ProjectConfiguration.toProto(): ProtoProjectConfiguration =
    ProtoProjectConfiguration.newBuilder()
        .setProjectId(projectId)
        .setConsent(consent.toProto())
        .setGeneral(general.toProto())
        .setIdentification(identification.toProto())
        .setSynchronization(synchronization.toProto())
        .also {
            if (face != null) it.face = face.toProto()
            if (fingerprint != null) it.fingerprint = fingerprint.toProto()
        }
        .build()

internal fun ProtoProjectConfiguration.toDomain(): ProjectConfiguration =
    ProjectConfiguration(
        projectId,
        general.toDomain(),
        hasFace().let { if (it) face.toDomain() else null },
        hasFingerprint().let { if (it) fingerprint.toDomain() else null },
        consent.toDomain(),
        identification.toDomain(),
        synchronization.toDomain(),
    )
