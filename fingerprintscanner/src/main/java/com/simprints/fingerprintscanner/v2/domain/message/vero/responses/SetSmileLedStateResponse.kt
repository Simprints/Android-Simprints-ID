package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetSmileLedStateResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_SMILE_LED_STATE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            SetSmileLedStateResponse(OperationResultCode.fromBytes(data))
    }
}
