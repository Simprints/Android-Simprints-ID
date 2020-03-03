package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetSupportedImageFormatsResponse(val supportedImageFormats: Set<ImageFormat>) : Un20Response(Un20MessageType.GetSupportedImageFormats) {

    override fun getDataBytes(): ByteArray = supportedImageFormats.map { it.getBytes() }.reduce { acc, bytes -> acc + bytes }

    companion object {
        fun fromBytes(data: ByteArray) =
            GetSupportedImageFormatsResponse(
                data.toList()
                    .map { ImageFormat.fromByte(it) }
                    .toSet()
            )
    }
}
