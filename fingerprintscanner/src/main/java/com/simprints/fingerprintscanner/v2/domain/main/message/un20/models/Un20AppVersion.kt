package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class Un20AppVersion(
    val apiMajorVersion: Short,
    val apiMinorVersion: Short,
    val firmwareMajorVersion: Short,
    val firmwareMinorVersion: Short
) {

    fun getBytes() = with(Un20MessageProtocol) {
        apiMajorVersion.toByteArray(byteOrder) +
            apiMinorVersion.toByteArray(byteOrder) +
            firmwareMajorVersion.toByteArray(byteOrder) +
            firmwareMinorVersion.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(Un20MessageProtocol) {
                Un20AppVersion(
                    apiMajorVersion = bytes.extract({ short }, 0..1),
                    apiMinorVersion = bytes.extract({ short }, 2..3),
                    firmwareMajorVersion = bytes.extract({ short }, 4..5),
                    firmwareMinorVersion = bytes.extract({ short }, 6..7)
                )
            }
    }
}
