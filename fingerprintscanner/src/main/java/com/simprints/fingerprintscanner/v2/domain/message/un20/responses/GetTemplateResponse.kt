package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetTemplateResponse(val templateType: TemplateType, val template: ByteArray) : Un20Response(Un20MessageType.GetTemplate(templateType.byte)) {

    companion object {
        fun fromBytes(minorResponseByte: Byte, data: ByteArray) =
            GetTemplateResponse(TemplateType.fromBytes(byteArrayOf(minorResponseByte)), data)
    }
}
