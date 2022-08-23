package com.simprints.infra.config.local.models

import com.simprints.infra.config.domain.models.DeviceConfiguration

internal fun DeviceConfiguration.toProto(): ProtoDeviceConfiguration =
    ProtoDeviceConfiguration.newBuilder()
        .setLanguage(language)
        .addAllModuleSelected(moduleSelected)
        .build()

internal fun ProtoDeviceConfiguration.toDomain(): DeviceConfiguration =
    DeviceConfiguration(language, moduleSelectedList)
