package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class GetImageCommand(
    val imageFormatData: ImageFormatData,
) : Un20Command(Un20MessageType.GetImage(imageFormatData.imageFormat.byte)) {
    override fun getDataBytes(): ByteArray = imageFormatData.getDataBytes()

    companion object {
        fun fromBytes(
            minorResponseByte: Byte,
            data: ByteArray,
        ) = GetImageCommand(ImageFormatData.fromBytes(minorResponseByte, data))
    }
}
