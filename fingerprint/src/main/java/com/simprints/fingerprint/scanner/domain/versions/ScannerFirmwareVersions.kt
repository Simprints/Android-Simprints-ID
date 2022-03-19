package com.simprints.fingerprint.scanner.domain.versions

import com.fasterxml.jackson.annotation.JsonProperty

data class ScannerFirmwareVersions(
    @JsonProperty val cypress: String,
    @JsonProperty val stm: String,
    @JsonProperty val un20: String
) {


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
