package com.simprints.fingerprintscanner.v2.domain.message.un20.models

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class Un20AppVersion(
    val majorVersionCode: Short,
    val minorVersionCode: Short,
    val firmwareVersion: Int,
    val magicBytes: ByteArray
) {

    fun getBytes() = with(Un20MessageProtocol) {
        majorVersionCode.toByteArray(byteOrder) +
            minorVersionCode.toByteArray(byteOrder) +
            firmwareVersion.toByteArray(byteOrder) +
            magicBytes
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(Un20MessageProtocol) {
                Un20AppVersion(
                    majorVersionCode = bytes.extract({ short }, 0..1),
                    minorVersionCode = bytes.extract({ short }, 2..3),
                    firmwareVersion = bytes.extract({ int }, 4..7),
                    magicBytes = bytes.sliceArray(8..11)
                )
            }

    }
}
