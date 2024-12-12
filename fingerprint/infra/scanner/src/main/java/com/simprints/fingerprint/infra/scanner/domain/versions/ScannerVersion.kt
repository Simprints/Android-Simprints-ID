package com.simprints.fingerprint.infra.scanner.domain.versions

import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration

/**
 * This class represents the unified versions of the modules on the scanner and the generation of the
 * vero scanner.
 *
 * @property hardwareVersion the hardwareVersion of the scanner
 * @property generation  the generation of the scanner
 * @property firmware  the unified firmware versions of the scanner's modules
 */

data class ScannerVersion(
    val hardwareVersion: String,
    val generation: ScannerGeneration,
    val firmware: ScannerFirmwareVersions,
)
