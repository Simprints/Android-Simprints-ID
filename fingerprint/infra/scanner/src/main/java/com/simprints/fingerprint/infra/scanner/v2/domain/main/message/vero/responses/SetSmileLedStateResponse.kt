package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

class SetSmileLedStateResponse(
    val operationResultCode: OperationResultCode,
) : VeroResponse(VeroMessageType.SET_SMILE_LED_STATE) {
    override fun getDataBytes(): ByteArray = byteArrayOf(operationResultCode.byte)

    companion object {
        fun fromBytes(data: ByteArray) = SetSmileLedStateResponse(OperationResultCode.fromBytes(data))
    }
}
