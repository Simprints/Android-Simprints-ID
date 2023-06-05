package com.simprints.fingerprintscanner.v2.domain.main.message.vero.events

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class Un20StateChangeEvent(val value: DigitalValue) : VeroEvent(VeroMessageType.UN20_STATE_CHANGE) {

    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) =
            Un20StateChangeEvent(DigitalValue.fromBytes(data))
    }
}
