package com.simprints.fingerprintscanner.v2.domain.message.un20.messages.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetSupportedTemplateTypesResponse(val supportedTemplateTypes: Set<TemplateType>) : Un20Response(Un20MessageType.GetSupportedTemplateTypes) {

    companion object {
        fun fromBytes(data: ByteArray) =
            GetSupportedTemplateTypesResponse(
                data.asList()
                    .map { TemplateType.fromBytes(byteArrayOf(it)) }
                    .toSet()
            )
    }
}
