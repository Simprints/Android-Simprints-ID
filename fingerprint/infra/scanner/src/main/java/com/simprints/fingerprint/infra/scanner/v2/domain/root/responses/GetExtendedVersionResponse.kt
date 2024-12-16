package com.simprints.fingerprint.infra.scanner.v2.domain.root.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation

class GetExtendedVersionResponse(
    val version: ExtendedVersionInformation,
) : RootResponse(RootMessageType.GET_EXTENDED_VERSION) {
    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetExtendedVersionResponse(
            ExtendedVersionInformation.fromBytes(data),
        )
    }
}
