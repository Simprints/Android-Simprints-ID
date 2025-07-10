package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException
import com.simprints.infra.config.store.models.SampleSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration.UpSynchronizationKind

internal fun UpSynchronizationConfiguration.toProto(): ProtoUpSynchronizationConfiguration = ProtoUpSynchronizationConfiguration
    .newBuilder()
    .setSimprints(simprints.toProto())
    .setCoSync(coSync.toProto())
    .build()

internal fun UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.toProto() =
    ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration
        .newBuilder()
        .setKind(kind.toProto())
        .setBatchSizes(batchSizes.toProto())
        .setImagesRequireUnmeteredConnection(imagesRequireUnmeteredConnection)
        .setFrequency(frequency.toProto())
        .build()

internal fun UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.toProto() =
    ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration
        .newBuilder()
        .setKind(kind.toProto())
        .build()

internal fun UpSynchronizationKind.toProto(): ProtoUpSynchronizationConfiguration.UpSynchronizationKind = when (this) {
    UpSynchronizationKind.NONE -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE
    UpSynchronizationKind.ONLY_ANALYTICS -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
    UpSynchronizationKind.ONLY_BIOMETRICS -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
    UpSynchronizationKind.ALL -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL
}

internal fun UpSynchronizationConfiguration.UpSyncBatchSizes.toProto() = ProtoUpSyncBatchSizes
    .newBuilder()
    .setSessions(sessions)
    .setEventUpSyncs(eventUpSyncs)
    .setEventDownSyncs(eventDownSyncs)
    .setSampleUpSyncs(sampleUpSyncs)
    .build()

internal fun SampleSynchronizationConfiguration.toProto() = ProtoSampleSynchronizationConfiguration
    .newBuilder()
    .setSignedUrlBatchSize(signedUrlBatchSize)
    .build()

internal fun ProtoUpSynchronizationConfiguration.toDomain() = UpSynchronizationConfiguration(
    simprints.toDomain(),
    coSync.toDomain(),
)

internal fun ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.toDomain() =
    UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(
        kind = kind.toDomain(),
        batchSizes = batchSizes.toDomain(),
        imagesRequireUnmeteredConnection = imagesRequireUnmeteredConnection,
        frequency = frequency.toDomain(),
    )

internal fun ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.toDomain() =
    UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(kind.toDomain())

internal fun ProtoUpSynchronizationConfiguration.UpSynchronizationKind.toDomain() = when (this) {
    ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE -> UpSynchronizationKind.NONE
    ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS -> UpSynchronizationKind.ONLY_ANALYTICS
    ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS -> UpSynchronizationKind.ONLY_BIOMETRICS
    ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL -> UpSynchronizationKind.ALL
    ProtoUpSynchronizationConfiguration.UpSynchronizationKind.UNRECOGNIZED -> throw InvalidProtobufEnumException(
        "invalid UpSynchronizationKind $name",
    )
}

internal fun ProtoUpSyncBatchSizes.toDomain(): UpSynchronizationConfiguration.UpSyncBatchSizes =
    UpSynchronizationConfiguration.UpSyncBatchSizes(sessions, eventUpSyncs, eventDownSyncs, sampleUpSyncs)

internal fun ProtoSampleSynchronizationConfiguration.toDomain() = SampleSynchronizationConfiguration(
    signedUrlBatchSize = signedUrlBatchSize,
)
