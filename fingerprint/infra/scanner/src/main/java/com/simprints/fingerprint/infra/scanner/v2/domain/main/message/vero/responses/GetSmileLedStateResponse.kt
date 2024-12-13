package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.SmileLedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

class GetSmileLedStateResponse(
    val smileLedState: SmileLedState,
) : VeroResponse(VeroMessageType.GET_SMILE_LED_STATE) {
    override fun getDataBytes(): ByteArray = smileLedState.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetSmileLedStateResponse(SmileLedState.fromBytes(data))
    }
}
