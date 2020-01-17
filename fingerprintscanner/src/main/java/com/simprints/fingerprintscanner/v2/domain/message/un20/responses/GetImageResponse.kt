package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetImageResponse(val imageData: ImageData) : Un20Response(Un20MessageType.GetImage(imageData.imageFormat.byte)) {

    override fun getDataBytes(): ByteArray = imageData.image + imageData.crcValue

    companion object {
        fun fromBytes(minorTypeByte: Byte, data: ByteArray) =
            GetImageResponse(
                ImageData(
                    ImageFormat.fromBytes(byteArrayOf(minorTypeByte)),
                    data.sliceArray(0 until data.size - 2),
                    data.sliceArray(data.size - 2 until data.size)
                )
            )
    }
}
