package com.simprints.infra.config.domain.models

data class DeviceConfiguration(
    var language: String,
    var selectedModules: List<String>,
    var fingersToCollect: List<Finger>,
    var lastInstructionId: String
)
