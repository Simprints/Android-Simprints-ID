package com.simprints.fingerprintscanner.v2.domain.message.un20.models

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.tools.toByteArray

class Dpi(val value: Short) {

    fun getBytes() = value.toByteArray()

    companion object {
        fun fromBytes(bytes: ByteArray) = with(Un20MessageProtocol) {
            Dpi(
                bytes.extract({ short })
            )
        }
    }
}
