package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.DeviceConfiguration

internal fun DeviceConfiguration.toProto(): ProtoDeviceConfiguration =
    ProtoDeviceConfiguration.newBuilder()
        .setLanguage(
            ProtoDeviceConfiguration.Language.newBuilder()
                .setLanguage(language)
                .build()
        )
        .addAllModuleSelected(moduleSelected)
        .setFingersToCollect(
            ProtoDeviceConfiguration.FingersToCollect.newBuilder()
                .addAllFingersToCollect(fingersToCollect.map { it.toProto() })
                .build()
        )
        .build()

internal fun ProtoDeviceConfiguration.toDomain(): DeviceConfiguration =
    DeviceConfiguration(
        language.language,
        moduleSelectedList,
        fingersToCollect.fingersToCollectList.map { it.toDomain() },
    )
