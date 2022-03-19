package com.simprints.fingerprint.scanner.domain.ota

data class DownloadableFirmwareVersion(
    val chip: Chip,
    val version: String
) {
    enum class Chip(val chipName: String) {
        CYPRESS("cypress"),
        STM("stm"),
        UN20("un20")
    }
}
