package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.Vero1Configuration

internal fun Vero1Configuration.toProto(): ProtoVero1Configuration = ProtoVero1Configuration
    .newBuilder()
    .setQualityThreshold(qualityThreshold)
    .build()

internal fun ProtoVero1Configuration.toDomain(): Vero1Configuration = Vero1Configuration(
    qualityThreshold,
)
