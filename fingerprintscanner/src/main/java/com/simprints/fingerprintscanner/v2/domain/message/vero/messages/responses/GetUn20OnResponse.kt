package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetUn20OnResponse(val value: DigitalValue) : VeroResponse(VeroMessageType.GET_UN20_ON) {

    companion object {
        fun fromBytes(data: ByteArray) =
            GetUn20OnResponse(DigitalValue.fromBytes(data))
    }
}
