package com.simprints.fingerprint.scanner.domain.versions

data class ScannerFirmwareVersions(val cypress: String,
                                   val stm: String,
                                   val un20: String) {


    operator fun compareTo(other: ScannerFirmwareVersions) = if (this == other) 0 else -1

    companion object {
        const val UNKNOWN_VERSION = ""
        val UNKNOWN = ScannerFirmwareVersions(
            cypress = "",
            stm = "",
            un20 = "",
        )
    }
}
