package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.tools.primitives.unsignedToInt

sealed class ImageFormatData(val imageFormat: ImageFormat, val extraData: ByteArray = byteArrayOf()) {
    object RAW : ImageFormatData(ImageFormat.RAW)
    object PNG : ImageFormatData(ImageFormat.PNG)
    class WSQ(val compression: Int) : ImageFormatData(ImageFormat.WSQ, byteArrayOf(compression.toByte()))

    fun getDataBytes() = extraData

    companion object {
        fun fromBytes(minorResponseByte: Byte, data: ByteArray): ImageFormatData =
            when (ImageFormat.fromByte(minorResponseByte)) {
                ImageFormat.RAW -> RAW
                ImageFormat.PNG -> PNG
                ImageFormat.WSQ -> WSQ(compression = data[0].unsignedToInt())
            }
    }
}
