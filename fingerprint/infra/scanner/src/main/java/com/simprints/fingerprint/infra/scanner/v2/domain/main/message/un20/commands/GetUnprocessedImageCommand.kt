package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

/**
 * Get unprocessed image command
 * take the image from scanner before any processing
 * @property imageFormatData
 * @constructor Create empty Get unprocessed image command
 */
class GetUnprocessedImageCommand(
    private val imageFormatData: ImageFormatData,
) : Un20Command(Un20MessageType.GetUnprocessedImage(imageFormatData.imageFormat.byte)) {
    override fun getDataBytes(): ByteArray = imageFormatData.getDataBytes()

    companion object {
        fun fromBytes(
            minorResponseByte: Byte,
            data: ByteArray,
        ) = GetUnprocessedImageCommand(ImageFormatData.fromBytes(minorResponseByte, data))
    }
}
