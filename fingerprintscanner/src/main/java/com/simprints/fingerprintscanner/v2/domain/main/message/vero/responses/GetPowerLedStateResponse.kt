package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class GetPowerLedStateResponse(val ledState: LedState) : VeroResponse(VeroMessageType.GET_POWER_LED_STATE) {

    override fun getDataBytes(): ByteArray = ledState.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) =
            GetPowerLedStateResponse(LedState.fromBytes(data))
    }
}
