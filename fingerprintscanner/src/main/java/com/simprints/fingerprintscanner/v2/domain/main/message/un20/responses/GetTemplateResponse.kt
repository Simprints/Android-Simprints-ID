package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf

class GetTemplateResponse(val templateData: TemplateData) : Un20Response(Un20MessageType.GetTemplate(templateData.templateType.byte)) {

    override fun getDataBytes(): ByteArray = byteArrayOf(templateData.template)

    companion object {
        fun fromBytes(minorResponseByte: Byte, data: ByteArray) =
            GetTemplateResponse(
                TemplateData(
                    templateType = TemplateType.fromBytes(byteArrayOf(minorResponseByte)),
                    template = data
                )
            )
    }
}
