package com.simprints.fingerprint.infra.scanner.domain.ota

import com.simprints.fingerprint.infra.scanner.domain.versions.ScannerFirmwareVersions.Companion.UNKNOWN_VERSION

/**
 * This class represents a new firmware version that is to be downloaded from the remote source.
 *
 * @property chip  the firmware chip this version is for
 * @property version  the firmware version to be downloaded
 */
data class DownloadableFirmwareVersion(
    val chip: Chip,
    val version: String,
) {
    /**
     * This enum class represent the different chips on the scanner
     */
    enum class Chip(
        val chipName: String,
    ) {
        CYPRESS("cypress"),
        STM("stm"),
        UN20("un20"),
    }

    fun toStringForApi(): String {
        val versionString = if (version == UNKNOWN_VERSION) "0.0" else version
        return "${chip.chipName}_$versionString"
    }
}
