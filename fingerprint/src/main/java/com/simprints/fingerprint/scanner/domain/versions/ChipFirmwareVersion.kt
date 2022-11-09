package com.simprints.fingerprint.scanner.domain.versions

/**
 * This class represents the version of the firmware that is running on the vero scanner
 */
@Deprecated(message = "Firmware versions are no longer represented in this form")
data class ChipFirmwareVersion(val major: Int, val minor: Int) : Comparable<ChipFirmwareVersion> {

    fun combined(): Int = (major shl 16) + minor.toShort()

    override operator fun compareTo(other: ChipFirmwareVersion) = this.combined() - other.combined()

    override fun toString() = "$major.$minor"

    companion object {
        val UNKNOWN = ChipFirmwareVersion(-1, -1)
    }
}
