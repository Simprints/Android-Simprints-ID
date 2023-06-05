package com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetSupportedTemplateTypesCommand : Un20Command(Un20MessageType.GetSupportedTemplateTypes) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetSupportedTemplateTypesCommand()
    }
}
