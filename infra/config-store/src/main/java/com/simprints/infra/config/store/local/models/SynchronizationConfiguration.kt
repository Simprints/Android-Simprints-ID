package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.SynchronizationConfiguration

internal fun SynchronizationConfiguration.toProto(): ProtoSynchronizationConfiguration = ProtoSynchronizationConfiguration
    .newBuilder()
    .setDown(down.toProto())
    .setUp(up.toProto())
    .setSamples(samples.toProto())
    .build()

internal fun ProtoSynchronizationConfiguration.toDomain(): SynchronizationConfiguration = SynchronizationConfiguration(
    up = up.toDomain(),
    down = down.toDomain(),
    samples = samples.toDomain(),
)
