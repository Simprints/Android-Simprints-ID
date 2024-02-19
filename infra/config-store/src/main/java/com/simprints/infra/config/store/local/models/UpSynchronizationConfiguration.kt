package com.simprints.infra.config.store.local.models

import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.config.store.exceptions.InvalidProtobufEnumException

internal fun UpSynchronizationConfiguration.toProto(): ProtoUpSynchronizationConfiguration =
    ProtoUpSynchronizationConfiguration.newBuilder()
        .setSimprints(simprints.toProto())
        .setCoSync(coSync.toProto())
        .setImagesRequireUnmeteredConnection(imagesRequireUnmeteredConnection)
        .build()

internal fun UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.toProto(): ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration =
    ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.newBuilder()
        .setKind(kind.toProto())
        .build()

internal fun UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.toProto(): ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration =
    ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.newBuilder()
        .setKind(kind.toProto())
        .build()

internal fun UpSynchronizationConfiguration.UpSynchronizationKind.toProto(): ProtoUpSynchronizationConfiguration.UpSynchronizationKind =
    when (this) {
        UpSynchronizationConfiguration.UpSynchronizationKind.NONE -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE
        UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
        UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        UpSynchronizationConfiguration.UpSynchronizationKind.ALL -> ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL
    }

internal fun ProtoUpSynchronizationConfiguration.toDomain(): UpSynchronizationConfiguration =
    UpSynchronizationConfiguration(simprints.toDomain(), coSync.toDomain(), imagesRequireUnmeteredConnection)

internal fun ProtoUpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration.toDomain(): UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration =
    UpSynchronizationConfiguration.SimprintsUpSynchronizationConfiguration(kind.toDomain())

internal fun ProtoUpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration.toDomain(): UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration =
    UpSynchronizationConfiguration.CoSyncUpSynchronizationConfiguration(kind.toDomain())

internal fun ProtoUpSynchronizationConfiguration.UpSynchronizationKind.toDomain(): UpSynchronizationConfiguration.UpSynchronizationKind =
    when (this) {
        ProtoUpSynchronizationConfiguration.UpSynchronizationKind.NONE -> UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
        ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS -> UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        ProtoUpSynchronizationConfiguration.UpSynchronizationKind.ALL -> UpSynchronizationConfiguration.UpSynchronizationKind.ALL
        ProtoUpSynchronizationConfiguration.UpSynchronizationKind.UNRECOGNIZED -> throw InvalidProtobufEnumException(
            "invalid UpSynchronizationKind $name"
        )
    }
