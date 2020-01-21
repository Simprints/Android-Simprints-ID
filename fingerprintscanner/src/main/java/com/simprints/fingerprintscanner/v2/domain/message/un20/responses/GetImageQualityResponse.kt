package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt

class GetImageQualityResponse(val imageQualityScore: Int) : Un20Response(Un20MessageType.GetImageQuality) {

    override fun getDataBytes(): ByteArray = byteArrayOf(imageQualityScore.toByte())

    companion object {
        fun fromBytes(data: ByteArray) = GetImageQualityResponse(data[0].unsignedToInt())
    }
}
