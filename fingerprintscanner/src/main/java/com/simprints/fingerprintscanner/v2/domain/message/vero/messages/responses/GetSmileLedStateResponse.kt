package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.SmileLedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetSmileLedStateResponse(val smileLedState: SmileLedState): VeroResponse(VeroMessageType.GET_SMILE_LED_STATE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            GetSmileLedStateResponse(SmileLedState.fromBytes(data))
    }
}
