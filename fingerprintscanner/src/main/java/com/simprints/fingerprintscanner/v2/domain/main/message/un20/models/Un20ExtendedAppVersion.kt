package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf as byteArrayOfAny

data class Un20ExtendedAppVersion(val versionAsString: String) {

    fun getBytes(): ByteArray {
        val bytes = versionAsString.toByteArray()
        return byteArrayOfAny(bytes.size, bytes)
    }

    companion object {
        fun fromString(version: String) = Un20ExtendedAppVersion(
            versionAsString = version
        )

        fun fromBytes(bytes: ByteArray) = Un20ExtendedAppVersion(
            versionAsString = String(bytes)
        )
    }
}
