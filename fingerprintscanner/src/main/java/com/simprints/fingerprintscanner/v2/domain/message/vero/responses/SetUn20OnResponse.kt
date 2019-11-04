package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class SetUn20OnResponse(val operationResultCode: OperationResultCode) : VeroResponse(VeroMessageType.SET_UN20_ON) {

    override fun getDataBytes(): ByteArray = byteArrayOf(operationResultCode.byte)

    companion object {
        fun fromBytes(data: ByteArray) =
            SetUn20OnResponse(OperationResultCode.fromBytes(data))
    }
}
