package com.simprints.fingerprint.scanner.domain.versions

data class ChipFirmwareVersion(val major: Int, val minor: Int) {

    fun combined(): Int = (major shl 16) + minor.toShort()

    operator fun compareTo(other: ChipFirmwareVersion) = this.combined() - other.combined()

    override fun toString() = "$major.$minor"

    companion object {
        val UNKNOWN = ChipFirmwareVersion(-1, -1)
    }
}
