package com.simprints.fingerprint.scanner.domain.ota

data class DownloadableFirmwareVersion(
    val chip: Chip,
    val version: String,
    val downloadUrl: String
) {
    enum class Chip(val chipName: String) {
        CYPRESS("cypress"),
        STM("stm"),
        UN20("un20")
    }
}
