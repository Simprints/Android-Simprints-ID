package com.simprints.fingerprintscanner.v2.domain.message.vero.events

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class Un20StateChangeEvent(val value: DigitalValue) : VeroEvent(VeroMessageType.UN20_STATE_CHANGE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            Un20StateChangeEvent(DigitalValue.fromBytes(data))
    }
}
