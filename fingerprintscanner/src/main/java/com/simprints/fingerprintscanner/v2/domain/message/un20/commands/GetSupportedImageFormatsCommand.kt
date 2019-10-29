package com.simprints.fingerprintscanner.v2.domain.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetSupportedImageFormatsCommand : Un20Command(Un20MessageType.GetSupportedImageFormats) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetSupportedImageFormatsCommand()
    }
}
