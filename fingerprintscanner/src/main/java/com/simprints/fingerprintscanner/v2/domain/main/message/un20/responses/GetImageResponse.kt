package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetImageResponse(val imageFormat: ImageFormat, val imageData: ImageData?) : Un20Response(Un20MessageType.GetImage(imageFormat.byte)) {

    override fun getDataBytes(): ByteArray = imageData?.let {
        it.image + with(Un20MessageProtocol) { it.crcValue.toByteArray() }
    } ?: byteArrayOf()

    companion object {
        fun fromBytes(minorTypeByte: Byte, data: ByteArray) =
            GetImageResponse(
                ImageFormat.fromByte(minorTypeByte),
                if (data.isNotEmpty()) ImageData(
                    data.sliceArray(0 until data.size - 4),
                    with(Un20MessageProtocol) {
                        data.sliceArray(data.size - 4 until data.size).extract({ int })
                    }
                ) else null
            )
    }
}
