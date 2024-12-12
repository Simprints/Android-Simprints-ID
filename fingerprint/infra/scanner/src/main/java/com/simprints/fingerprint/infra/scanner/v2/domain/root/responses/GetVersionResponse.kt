package com.simprints.fingerprint.infra.scanner.v2.domain.root.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.UnifiedVersionInformation

class GetVersionResponse(
    val version: UnifiedVersionInformation,
) : RootResponse(RootMessageType.GET_VERSION) {
    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetVersionResponse(
            UnifiedVersionInformation.fromBytes(data),
        )
    }
}
