package com.simprints.fingerprint.scanner.domain.versions

import com.fasterxml.jackson.annotation.JsonProperty

data class ScannerFirmwareVersions(
    @JsonProperty val cypress: String,
    @JsonProperty val stm: String,
    @JsonProperty val un20: String
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
