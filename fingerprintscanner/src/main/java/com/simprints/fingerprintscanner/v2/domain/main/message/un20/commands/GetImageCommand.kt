package com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.ImageFormatData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetImageCommand(val imageFormatData: ImageFormatData) : Un20Command(Un20MessageType.GetImage(imageFormatData.imageFormat.byte)) {

    companion object {
        fun fromBytes(minorResponseByte: Byte, data: ByteArray) =
            GetImageCommand(ImageFormatData.fromBytes(minorResponseByte, data))
    }
}
