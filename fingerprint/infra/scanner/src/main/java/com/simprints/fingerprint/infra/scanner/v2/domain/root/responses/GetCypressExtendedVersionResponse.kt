package com.simprints.fingerprint.infra.scanner.v2.domain.root.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion

class GetCypressExtendedVersionResponse(
    val version: CypressExtendedFirmwareVersion,
) : RootResponse(RootMessageType.GET_CYPRESS_EXTENDED_VERSION) {
    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetCypressExtendedVersionResponse(
            CypressExtendedFirmwareVersion.fromString(String(data)),
        )
    }
}
