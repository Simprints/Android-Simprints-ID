package com.simprints.fingerprint.infra.scanner.domain.versions

data class ScannerFirmwareVersions(
    val cypress: String,
    val stm: String,
    val un20: String,
) {
    companion object {
        const val UNKNOWN_VERSION = ""
        val UNKNOWN = ScannerFirmwareVersions(
            cypress = "",
            stm = "",
            un20 = "",
        )
    }
}
