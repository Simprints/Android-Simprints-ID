package com.simprints.infra.config.domain.models

data class DeviceConfiguration(
    var language: String,
    var moduleSelected: List<String>,
    var fingersToCollect: List<Finger>
)
