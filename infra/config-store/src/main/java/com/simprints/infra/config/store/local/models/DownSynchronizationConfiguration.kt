package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.local.models.ProtoDownSynchronizationConfiguration.ProtoSimprintsDownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration
import com.simprints.infra.config.store.models.DownSynchronizationConfiguration.SimprintsDownSynchronizationConfiguration

internal fun DownSynchronizationConfiguration.toProto(): ProtoDownSynchronizationConfiguration = ProtoDownSynchronizationConfiguration
    .newBuilder()
    .setSimprints(simprints.toProto())
    .build()

internal fun SimprintsDownSynchronizationConfiguration.toProto(): ProtoSimprintsDownSynchronizationConfiguration {
    val isTokenized = moduleOptions.any { it is TokenizableString.Tokenized }
    return ProtoSimprintsDownSynchronizationConfiguration
        .newBuilder()
        .setPartitionType(partitionType.toProto())
        .setMaxNbOfModules(maxNbOfModules)
        .addAllModuleOptions(moduleOptions.values())
        .setIsTokenized(isTokenized)
        .setMaxAge(maxAge)
        .setFrequency(frequency.toProto())
        .build()
}

internal fun DownSynchronizationConfiguration.PartitionType.toProto(): ProtoDownSynchronizationConfiguration.PartitionType = when (this) {
    DownSynchronizationConfiguration.PartitionType.PROJECT -> ProtoDownSynchronizationConfiguration.PartitionType.PROJECT
    DownSynchronizationConfiguration.PartitionType.MODULE -> ProtoDownSynchronizationConfiguration.PartitionType.MODULE
    DownSynchronizationConfiguration.PartitionType.USER -> ProtoDownSynchronizationConfiguration.PartitionType.USER
}

internal fun ProtoDownSynchronizationConfiguration.toDomain() = DownSynchronizationConfiguration(
    simprints = simprints.toDomain(),
)

internal fun ProtoSimprintsDownSynchronizationConfiguration.toDomain() = SimprintsDownSynchronizationConfiguration(
    partitionType = partitionType.toDomain(),
    maxNbOfModules = maxNbOfModules,
    moduleOptions = moduleOptionsList.map {
        if (isTokenized) it.asTokenizableEncrypted() else it.asTokenizableRaw()
    },
    maxAge = maxAge,
    frequency = frequency.toDomain(),
)

internal fun ProtoDownSynchronizationConfiguration.PartitionType.toDomain(): DownSynchronizationConfiguration.PartitionType = when (this) {
    ProtoDownSynchronizationConfiguration.PartitionType.PROJECT -> DownSynchronizationConfiguration.PartitionType.PROJECT
    ProtoDownSynchronizationConfiguration.PartitionType.MODULE -> DownSynchronizationConfiguration.PartitionType.MODULE
    ProtoDownSynchronizationConfiguration.PartitionType.USER -> DownSynchronizationConfiguration.PartitionType.USER
    ProtoDownSynchronizationConfiguration.PartitionType.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid PartitionType $name",
    )
}
