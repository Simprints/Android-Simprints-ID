package com.simprints.fingerprintscanner.v2.domain.message.un20.models

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class Dpi(val value: Short) {

    fun getBytes() = value.toByteArray(Un20MessageProtocol.byteOrder)

    companion object {
        fun fromBytes(bytes: ByteArray) = with(Un20MessageProtocol) {
            Dpi(
                bytes.extract({ short })
            )
        }
    }
}
