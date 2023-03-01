package com.simprints.infra.config.domain.models

data class DeviceConfiguration(
    var language: String,
    var selectedModules: List<String>,
    var lastInstructionId: String
)
