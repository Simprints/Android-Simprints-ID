package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol

class BatteryVoltage(val milliVolts: Short) {

    fun getBytes() = with(VeroMessageProtocol) {
        milliVolts.toByteArray()
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                BatteryVoltage(bytes.extract({ short }))
            }
    }
}
