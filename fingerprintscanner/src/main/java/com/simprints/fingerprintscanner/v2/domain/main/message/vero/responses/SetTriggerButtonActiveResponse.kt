package com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.VeroMessageType

class SetTriggerButtonActiveResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_TRIGGER_BUTTON_ACTIVE) {

    override fun getDataBytes(): ByteArray = byteArrayOf(operationResultCode.byte)

    companion object {
        fun fromBytes(data: ByteArray) =
            SetTriggerButtonActiveResponse(OperationResultCode.fromBytes(data))
    }
}
