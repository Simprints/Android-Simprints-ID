package com.simprints.fingerprintscanner.v2.domain.root.models

data class ScannerInformation(
    val hardwareVersion: String,
    val firmwareVersions: ScannerVersionInfo
)
