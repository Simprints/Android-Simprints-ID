package com.simprints.infra.config.store.local.models

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.core.domain.tokenization.asTokenizedRaw
import com.simprints.core.domain.tokenization.values
import com.simprints.infra.config.store.models.DeviceConfiguration

internal fun DeviceConfiguration.toProto(): ProtoDeviceConfiguration {
    val isTokenized = selectedModules.any { it is TokenizableString.Tokenized }
    return ProtoDeviceConfiguration.newBuilder()
        .setLanguage(
            ProtoDeviceConfiguration.Language.newBuilder()
                .setLanguage(language)
                .build()
        )
        .addAllModuleSelected(selectedModules.values())
        .setLastInstructionId(lastInstructionId)
        .setIsTokenized(isTokenized)
        .build()
}

internal fun ProtoDeviceConfiguration.toDomain(): DeviceConfiguration =
    DeviceConfiguration(
        language = language.language,
        selectedModules = moduleSelectedList.map {
            if (isTokenized) it.asTokenizedEncrypted() else it.asTokenizedRaw()
        },
        lastInstructionId = lastInstructionId
    )
