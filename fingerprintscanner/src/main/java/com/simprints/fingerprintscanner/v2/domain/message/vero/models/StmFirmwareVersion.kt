package com.simprints.fingerprintscanner.v2.domain.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class StmFirmwareVersion(
    val majorVersionCode: Short,
    val minorVersionCode: Short,
    val firmwareVersion: Short,
    val magicBytes: Short
) {

    fun getBytes() = with(VeroMessageProtocol) {
        majorVersionCode.toByteArray(byteOrder) +
            minorVersionCode.toByteArray(byteOrder) +
            firmwareVersion.toByteArray(byteOrder) +
            magicBytes.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                StmFirmwareVersion(
                    majorVersionCode = bytes.extract({ short }, 0..1),
                    minorVersionCode = bytes.extract({ short }, 2..3),
                    firmwareVersion = bytes.extract({ short }, 4..5),
                    magicBytes = bytes.extract({ short }, 6..7)
                )
            }
    }
}
