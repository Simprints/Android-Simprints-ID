package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.domain.tokenization.asTokenizableRaw
import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.models.DeviceConfiguration

internal fun DeviceConfiguration.toProto(): ProtoDeviceConfiguration {
    val isTokenized = selectedModules.any { it is TokenizableString.Tokenized }
    return ProtoDeviceConfiguration
        .newBuilder()
        .setLanguage(
            ProtoDeviceConfiguration.Language
                .newBuilder()
                .setLanguage(language)
                .build(),
        ).addAllModuleSelected(selectedModules.values())
        .setLastInstructionId(lastInstructionId)
        .setIsTokenized(isTokenized)
        .build()
}

internal fun ProtoDeviceConfiguration.toDomain(): DeviceConfiguration = DeviceConfiguration(
    language = language.language,
    selectedModules = moduleSelectedList.map {
        if (isTokenized) it.asTokenizableEncrypted() else it.asTokenizableRaw()
    },
    lastInstructionId = lastInstructionId,
)
