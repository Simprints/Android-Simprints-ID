package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetBluetoothLedStateResponse(val ledState: LedState) : VeroResponse(VeroMessageType.GET_BLUETOOTH_LED_STATE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            GetBluetoothLedStateResponse(LedState.fromBytes(data))
    }
}
