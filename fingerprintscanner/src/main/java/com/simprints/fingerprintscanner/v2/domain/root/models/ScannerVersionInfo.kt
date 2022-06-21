package com.simprints.fingerprintscanner.v2.domain.root.models

sealed class ScannerVersionInfo {
    data class LegacyVersionInfo(val versionInfo: UnifiedVersionInformation): ScannerVersionInfo()
    data class ExtendedVersionInfo(val versionInfo: ExtendedVersionInformation): ScannerVersionInfo()
}
