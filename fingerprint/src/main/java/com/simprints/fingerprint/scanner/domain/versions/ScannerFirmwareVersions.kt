package com.simprints.fingerprint.scanner.domain.versions

data class ScannerFirmwareVersions(val cypress: ChipFirmwareVersion,
                                   val stm: ChipFirmwareVersion,
                                   val un20: ChipFirmwareVersion) {

    fun combined(): Int = cypress.combined() + stm.combined() + un20.combined()

    operator fun compareTo(other: ScannerFirmwareVersions) = this.combined() - other.combined()

    companion object {
        val UNKNOWN = ScannerFirmwareVersions(ChipFirmwareVersion.UNKNOWN, ChipFirmwareVersion.UNKNOWN, ChipFirmwareVersion.UNKNOWN)
    }
}
