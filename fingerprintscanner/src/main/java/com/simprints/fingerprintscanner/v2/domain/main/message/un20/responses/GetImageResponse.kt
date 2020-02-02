package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetImageResponse(val imageData: ImageData) : Un20Response(Un20MessageType.GetImage(imageData.imageFormat.byte)) {

    override fun getDataBytes(): ByteArray = imageData.image +
        with(Un20MessageProtocol) { imageData.crcValue.toByteArray() }

    companion object {
        fun fromBytes(minorTypeByte: Byte, data: ByteArray) =
            GetImageResponse(
                ImageData(
                    ImageFormat.fromByte(minorTypeByte),
                    data.sliceArray(0 until data.size - 4),
                    with(Un20MessageProtocol) {
                        data.sliceArray(data.size - 4 until data.size).extract({ int })
                    }
                )
            )
    }
}
