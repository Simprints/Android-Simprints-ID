package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class BatteryPercentCharge(val percentCharge: Short) {

    fun getBytes() = with(VeroMessageProtocol) {
        percentCharge.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                BatteryPercentCharge(
                    bytes.extract({ short }, 0..1)
                )
            }
    }
}
