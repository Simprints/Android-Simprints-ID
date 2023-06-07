package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprintscanner.v2.tools.primitives.byteArrayOf

class GetTemplateResponse(val templateType: TemplateType, val templateData: TemplateData?) : Un20Response(Un20MessageType.GetTemplate(templateType.byte)) {

    override fun getDataBytes(): ByteArray = templateData?.let { byteArrayOf(it.template) }
        ?: byteArrayOf()

    companion object {
        fun fromBytes(minorResponseByte: Byte, data: ByteArray) =
            GetTemplateResponse(
                TemplateType.fromBytes(byteArrayOf(minorResponseByte)),
                if (data.isNotEmpty()) TemplateData(template = data) else null
            )
    }
}
