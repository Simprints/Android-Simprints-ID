package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol

class BatteryCurrent(val milliAmps: Short) { // Note that this will be negative when discharging

    fun getBytes() = with(VeroMessageProtocol) {
        milliAmps.toByteArray()
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                BatteryCurrent(bytes.extract({ short }))
            }
    }
}
