package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.local.models.ProtoSynchronizationConfiguration.Frequency
import com.simprints.infra.config.store.models.SynchronizationConfiguration

internal fun SynchronizationConfiguration.toProto(): ProtoSynchronizationConfiguration = ProtoSynchronizationConfiguration
    .newBuilder()
    .setFrequency(frequency.toProto())
    .setDown(down.toProto())
    .setUp(up.toProto())
    .setSamples(samples.toProto())
    .build()

internal fun SynchronizationConfiguration.Frequency.toProto(): Frequency = when (this) {
    SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC -> Frequency.ONLY_PERIODICALLY_UP_SYNC
    SynchronizationConfiguration.Frequency.PERIODICALLY -> Frequency.PERIODICALLY
    SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START -> Frequency.PERIODICALLY_AND_ON_SESSION_START
}

internal fun ProtoSynchronizationConfiguration.toDomain(): SynchronizationConfiguration = SynchronizationConfiguration(
    frequency = frequency.toDomain(),
    up = up.toDomain(),
    down = down.toDomain(),
    samples = samples.toDomain(),
)

internal fun Frequency.toDomain(): SynchronizationConfiguration.Frequency = when (this) {
    Frequency.ONLY_PERIODICALLY_UP_SYNC -> SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
    Frequency.PERIODICALLY -> SynchronizationConfiguration.Frequency.PERIODICALLY
    Frequency.PERIODICALLY_AND_ON_SESSION_START -> SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
    Frequency.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid Frequency $name")
}
