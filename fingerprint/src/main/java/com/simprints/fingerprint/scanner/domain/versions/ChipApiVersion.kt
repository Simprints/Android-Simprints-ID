package com.simprints.fingerprint.scanner.domain.versions


/**
 * This class represents the version of the api that SID communicates with, i.e. the version of the
 * api that the android app uses to communicate with the scanner.
 */
@Deprecated(message = "The firmware api version is no longer being used")
data class ChipApiVersion(val major: Int, val minor: Int) {

    fun combined(): Int = (major shl 16) + minor.toShort()

    operator fun compareTo(other: ChipApiVersion) = this.combined() - other.combined()

    override fun toString() = "$major.$minor"

    companion object {
        val UNKNOWN = ChipApiVersion(-1, -1)
    }
}
