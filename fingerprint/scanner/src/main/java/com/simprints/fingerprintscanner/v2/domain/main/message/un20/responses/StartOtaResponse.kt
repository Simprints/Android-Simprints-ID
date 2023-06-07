package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.OperationResultCode
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class StartOtaResponse(val operationResultCode: OperationResultCode) : Un20Response(Un20MessageType.StartOta) {

    companion object {
        fun fromBytes(data: ByteArray) = StartOtaResponse(OperationResultCode.fromBytes(data))
    }
}
