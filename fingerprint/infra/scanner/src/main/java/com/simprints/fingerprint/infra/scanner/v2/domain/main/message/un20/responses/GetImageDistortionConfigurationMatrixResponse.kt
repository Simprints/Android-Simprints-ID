package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class GetImageDistortionConfigurationMatrixResponse(
    val imageConfigurationMatrix: ByteArray?,
) : Un20Response(Un20MessageType.GetImageDistortionConfigurationMatrix) {
    override fun getDataBytes(): ByteArray = imageConfigurationMatrix?.let { imageConfigurationMatrix }
        ?: byteArrayOf()

    companion object {
        fun fromBytes(data: ByteArray) = GetImageDistortionConfigurationMatrixResponse(
            if (data.isNotEmpty()) {
                data
            } else {
                null
            },
        )
    }
}
