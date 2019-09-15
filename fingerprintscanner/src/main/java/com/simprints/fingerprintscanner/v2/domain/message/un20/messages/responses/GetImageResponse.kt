package com.simprints.fingerprintscanner.v2.domain.message.un20.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetImageResponse(val imageFormat: ImageFormat, val image: ByteArray) : Un20Response(Un20MessageType.GetImage(imageFormat.byte)) {

    companion object {
        fun fromBytes(minorTypeByte: Byte, data: ByteArray) =
            GetImageResponse(ImageFormat.fromBytes(byteArrayOf(minorTypeByte)), data)
    }
}
