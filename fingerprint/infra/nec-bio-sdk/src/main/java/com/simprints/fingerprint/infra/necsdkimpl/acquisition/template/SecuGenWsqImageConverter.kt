package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import SecuGen.FDxSDKPro.SGWSQLib
import javax.inject.Inject


class SecuGenWsqImageConverter @Inject constructor(private val sgwsqLib: SGWSQLib) {

    data class RawImage(
        val width: Int,
        val height: Int,
        val depth: Int,
        val ppi: Int,
        @Suppress("ArrayInDataClass") val bytes: ByteArray,
    )

    fun toRaw(wsqImage: ByteArray): RawImage {

        val wsqLen = wsqImage.size
        val imageOutSize = IntArray(1)

        sgwsqLib.SGWSQGetDecodedImageSize(imageOutSize, wsqImage, wsqLen)

        val rawImageOut = ByteArray(imageOutSize[0])
        val decodeWidth = IntArray(1)
        val decodeHeight = IntArray(1)
        val decodePixelDepth = IntArray(1)
        val decodePPI = IntArray(1)
        val decodeLossyFlag = IntArray(1)

        sgwsqLib.SGWSQDecode(
            rawImageOut,
            decodeWidth,
            decodeHeight,
            decodePixelDepth,
            decodePPI,
            decodeLossyFlag,
            wsqImage,
            wsqLen
        )

        return RawImage(
            decodeWidth[0], decodeHeight[0], decodePixelDepth[0], decodePPI[0], rawImageOut
        )

    }

    fun toWsq(rawImage: ByteArray, width: Int, height: Int, resolution: Int): ByteArray {

        val encodePixelDepth = 8 // 8 bits per pixel for grayscale image
        val wsqImageOutSize = IntArray(1)
        sgwsqLib.SGWSQGetEncodedImageSize(
            wsqImageOutSize,
            SGWSQLib.BITRATE_15_TO_1,// 15:1 compression ratio is the default value used in the scanner for encoding
            rawImage,
            width,
            height,
            encodePixelDepth,
            resolution
        )

        val wsqImageOut = ByteArray(wsqImageOutSize[0])
        sgwsqLib.SGWSQEncode(
            wsqImageOut,
            SGWSQLib.BITRATE_15_TO_1,
            rawImage,
            width,
            height,
            encodePixelDepth,
            resolution
        )
        return wsqImageOut
    }
}
