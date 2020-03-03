package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol

class BatteryTemperature(val deciKelvin: Short) {

    fun getBytes() = with(VeroMessageProtocol) {
        deciKelvin.toByteArray()
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                BatteryTemperature(bytes.extract({ short }))
            }
    }
}
