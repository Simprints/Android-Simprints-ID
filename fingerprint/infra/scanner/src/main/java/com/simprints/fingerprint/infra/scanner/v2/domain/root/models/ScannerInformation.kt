package com.simprints.fingerprint.infra.scanner.v2.domain.root.models

data class ScannerInformation(
    val hardwareVersion: String,
    val firmwareVersions: ExtendedVersionInformation,
)
