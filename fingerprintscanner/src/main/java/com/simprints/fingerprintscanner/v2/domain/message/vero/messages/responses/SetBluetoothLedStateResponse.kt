package com.simprints.fingerprintscanner.v2.domain.message.vero.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetBluetoothLedStateResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_BLUETOOTH_LED_STATE) {

    companion object {
        fun fromBytes(data: ByteArray) =
            SetBluetoothLedStateResponse(OperationResultCode.fromBytes(data))
    }
}
