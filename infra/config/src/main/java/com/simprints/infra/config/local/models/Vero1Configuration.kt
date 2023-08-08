package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.Vero1Configuration

internal fun Vero1Configuration.toProto(): ProtoVero1Configuration =
    ProtoVero1Configuration.newBuilder()
        .setQualityThreshold(qualityThreshold)
        .build()

internal fun ProtoVero1Configuration.toDomain(): Vero1Configuration =
    Vero1Configuration(
        qualityThreshold,
    )
