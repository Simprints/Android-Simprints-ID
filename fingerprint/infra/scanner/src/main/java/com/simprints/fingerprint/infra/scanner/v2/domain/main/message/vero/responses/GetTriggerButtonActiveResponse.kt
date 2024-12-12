package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

class GetTriggerButtonActiveResponse(
    val value: DigitalValue,
) : VeroResponse(VeroMessageType.GET_TRIGGER_BUTTON_ACTIVE) {
    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) = GetTriggerButtonActiveResponse(DigitalValue.fromBytes(data))
    }
}
