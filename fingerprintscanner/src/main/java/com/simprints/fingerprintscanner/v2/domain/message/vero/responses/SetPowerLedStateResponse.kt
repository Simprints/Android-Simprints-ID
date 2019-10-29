package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetPowerLedStateResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_POWER_LED_STATE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            SetPowerLedStateResponse(OperationResultCode.fromBytes(data))
    }
}
