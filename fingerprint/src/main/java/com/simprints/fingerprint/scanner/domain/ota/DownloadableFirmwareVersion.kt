package com.simprints.fingerprint.scanner.domain.ota

import com.simprints.fingerprint.scanner.domain.versions.ScannerFirmwareVersions.Companion.UNKNOWN_VERSION

data class DownloadableFirmwareVersion(
    val chip: Chip,
    val version: String
) {
    enum class Chip(val chipName: String) {
        CYPRESS("cypress"),
        STM("stm"),
        UN20("un20")
    }

    fun toStringForApi(): String {
        val versionString = if (version == UNKNOWN_VERSION) "0.0" else version
        return "${chip.chipName}_$versionString"
    }
}
