package com.simprints.fingerprint.controllers.core.network

import com.simprints.fingerprint.scanner.domain.versions.ChipFirmwareVersion

data class DownloadableFirmwareVersion(
    val chip: Chip,
    val version: ChipFirmwareVersion,
    val downloadUrl: String
)

enum class Chip(val chipName: String) {
    CYPRESS("cypress"),
    STM("stm"),
    UN20("un20")
}
