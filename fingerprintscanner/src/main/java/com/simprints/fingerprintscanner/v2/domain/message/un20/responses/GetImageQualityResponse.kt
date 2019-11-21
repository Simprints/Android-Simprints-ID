package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

class GetImageQualityResponse(val imageQuality: Short): Un20Response(Un20MessageType.GetImageQuality) {

    override fun getDataBytes(): ByteArray = imageQuality.toByteArray(Un20MessageProtocol.byteOrder)

    companion object {
        fun fromBytes(data: ByteArray) = with(Un20MessageProtocol) {
            GetImageQualityResponse(data.extract({ short }))
        }
    }
}
