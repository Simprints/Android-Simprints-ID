package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

class GetUn20OnResponse(
    val value: DigitalValue,
) : VeroResponse(VeroMessageType.GET_UN20_ON) {
    override fun getDataBytes(): ByteArray = byteArrayOf(value.byte)

    companion object {
        fun fromBytes(data: ByteArray) = GetUn20OnResponse(DigitalValue.fromBytes(data))
    }
}
