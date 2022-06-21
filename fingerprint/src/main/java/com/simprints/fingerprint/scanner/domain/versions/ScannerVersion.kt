package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.fingerprint.scanner.domain.ScannerGeneration

data class ScannerVersion(
    val hardwareVersion: String,
    val generation: ScannerGeneration,
    val firmware: ScannerFirmwareVersions
)
