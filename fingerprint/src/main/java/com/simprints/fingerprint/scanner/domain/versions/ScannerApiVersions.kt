package com.simprints.fingerprint.scanner.domain.versions

data class ScannerApiVersions(val cypress: ChipApiVersion,
                              val stm: ChipApiVersion,
                              val un20: ChipApiVersion) {

    fun combined(): Int = cypress.combined() + stm.combined() + un20.combined()

    operator fun compareTo(other: ScannerApiVersions) = this.combined() - other.combined()

    companion object {
        val UNKNOWN = ScannerApiVersions(ChipApiVersion.UNKNOWN, ChipApiVersion.UNKNOWN, ChipApiVersion.UNKNOWN)
    }
}
