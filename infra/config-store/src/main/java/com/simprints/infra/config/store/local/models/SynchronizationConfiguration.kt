package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.SynchronizationConfiguration

internal fun SynchronizationConfiguration.toProto(): ProtoSynchronizationConfiguration = ProtoSynchronizationConfiguration
    .newBuilder()
    .setFrequency(frequency.toProto())
    .setDown(down.toProto())
    .setUp(up.toProto())
    .build()

internal fun SynchronizationConfiguration.Frequency.toProto(): ProtoSynchronizationConfiguration.Frequency = when (this) {
    SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC -> ProtoSynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
    SynchronizationConfiguration.Frequency.PERIODICALLY -> ProtoSynchronizationConfiguration.Frequency.PERIODICALLY
    SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START -> ProtoSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
}

internal fun ProtoSynchronizationConfiguration.toDomain(): SynchronizationConfiguration = SynchronizationConfiguration(
    frequency.toDomain(),
    up.toDomain(),
    down.toDomain(),
)

internal fun ProtoSynchronizationConfiguration.Frequency.toDomain(): SynchronizationConfiguration.Frequency = when (this) {
    ProtoSynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC -> SynchronizationConfiguration.Frequency.ONLY_PERIODICALLY_UP_SYNC
    ProtoSynchronizationConfiguration.Frequency.PERIODICALLY -> SynchronizationConfiguration.Frequency.PERIODICALLY
    ProtoSynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START -> SynchronizationConfiguration.Frequency.PERIODICALLY_AND_ON_SESSION_START
    ProtoSynchronizationConfiguration.Frequency.UNRECOGNIZED -> throw InvalidProtobufEnumException("invalid Frequency $name")
}
