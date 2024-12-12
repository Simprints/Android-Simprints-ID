package com.simprints.infra.config.store.models

import com.simprints.core.domain.tokenization.TokenizableString

data class DeviceConfiguration(
    var language: String,
    var selectedModules: List<TokenizableString>,
    var lastInstructionId: String,
)
