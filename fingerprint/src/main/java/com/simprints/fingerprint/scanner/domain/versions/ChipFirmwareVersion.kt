package com.simprints.fingerprint.scanner.domain.versions

data class ChipFirmwareVersion(val major: Int, val minor: Int) : Comparable<ChipFirmwareVersion> {

    fun combined(): Int = (major shl 16) + minor.toShort()

    override operator fun compareTo(other: ChipFirmwareVersion) = this.combined() - other.combined()

    override fun toString() = "$major.$minor"

    companion object {
        val UNKNOWN = ChipFirmwareVersion(-1, -1)

        /** @throws IllegalArgumentException */
        fun fromString(s: String) =
            try {
                s.split(".").let { ChipFirmwareVersion(it[0].toInt(), it[1].toInt()) }
            } catch (e: Exception) {
                throw IllegalArgumentException("Incorrect ChipFirmwareVersion String format: $s", e)
            }
    }
}
