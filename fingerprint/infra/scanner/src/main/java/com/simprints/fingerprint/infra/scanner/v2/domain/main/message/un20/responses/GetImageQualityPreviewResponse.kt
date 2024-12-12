package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.unsignedToInt

class GetImageQualityPreviewResponse(
    val imageQualityScore: Int?,
) : Un20Response(Un20MessageType.GetImageQualityPreview) {
    override fun getDataBytes(): ByteArray = imageQualityScore?.let { byteArrayOf(it.toByte()) }
        ?: byteArrayOf()

    companion object {
        fun fromBytes(data: ByteArray) = GetImageQualityPreviewResponse(
            if (data.isNotEmpty()) {
                data[0].unsignedToInt()
            } else {
                null
            },
        )
    }
}
