package com.simprints.fingerprintscanner.v2.domain.message.un20.models

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol

class Un20AppVersion(
    val majorVersionCode: Short,
    val minorVersionCode: Short,
    val firmwareVersion: Int,
    val magicBytes: ByteArray
) {
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
