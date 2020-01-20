package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

class ImageData(
    val imageFormat: ImageFormat,
    val image: ByteArray,
    val crcValue: ByteArray
)
