package com.simprints.fingerprintscanner.v2.domain.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol

class FirmwareVersion(
    val majorVersionCode: Short,
    val minorVersionCode: Short,
    val firmwareVersion: Int,
    val magicBytes: ByteArray
) {
    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                FirmwareVersion(
                    majorVersionCode = bytes.extract({ short }, 0..1),
                    minorVersionCode = bytes.extract({ short }, 2..3),
                    firmwareVersion = bytes.extract({ int }, 4..7),
                    magicBytes = bytes.sliceArray(8..11)
                )
            }

    }
}
