package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.fingerprint.scanner.domain.ScannerGeneration

data class ScannerVersion(val generation: ScannerGeneration,
                          val firmware: ScannerFirmwareVersions,
                          val api: ScannerApiVersions) {

    /**
     * @throws IllegalArgumentException if generation is [ScannerGeneration.VERO_2] and any api
     * version is [ChipApiVersion.UNKNOWN] or if any firmware version is [ChipFirmwareVersion.UNKNOWN]
     */
    fun computeMasterVersion(): Long =
        when (generation) {
            ScannerGeneration.VERO_1 -> {
                TODO("Compute master version for Vero 1")
            }
            ScannerGeneration.VERO_2 -> {
                if (listOf(api.cypress, api.stm, api.un20).any { it == ChipApiVersion.UNKNOWN } ||
                    listOf(firmware.cypress, firmware.stm, firmware.un20).any { it == ChipFirmwareVersion.UNKNOWN }) {
                    throw IllegalArgumentException("Scanner version contains unknown quantity when computing master version: $this")
                }

                api.combined() * (2L shl 32) + firmware.combined()
            }
        }
}
