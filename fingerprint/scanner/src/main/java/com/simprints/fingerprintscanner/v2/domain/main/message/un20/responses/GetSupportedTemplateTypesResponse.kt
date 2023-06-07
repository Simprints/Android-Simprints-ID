package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class GetSupportedTemplateTypesResponse(val supportedTemplateTypes: Set<TemplateType>) : Un20Response(Un20MessageType.GetSupportedTemplateTypes) {

    override fun getDataBytes(): ByteArray = supportedTemplateTypes.map { it.getBytes() }.reduce { acc, bytes -> acc + bytes }

    companion object {
        fun fromBytes(data: ByteArray) =
            GetSupportedTemplateTypesResponse(
                data.asList()
                    .map { TemplateType.fromBytes(byteArrayOf(it)) }
                    .toSet()
            )
    }
}
