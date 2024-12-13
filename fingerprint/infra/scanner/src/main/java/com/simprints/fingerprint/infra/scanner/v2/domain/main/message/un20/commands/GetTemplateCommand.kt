package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class GetTemplateCommand(
    val templateType: TemplateType,
) : Un20Command(Un20MessageType.GetTemplate(templateType.byte)) {
    companion object {
        fun fromBytes(
            minorResponseByte: Byte,
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetTemplateCommand(TemplateType.fromBytes(byteArrayOf(minorResponseByte)))
    }
}
