package com.simprints.fingerprint.infra.scanner.v2.domain.root.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion

class GetCypressVersionResponse(
    val version: CypressFirmwareVersion,
) : RootResponse(RootMessageType.GET_CYPRESS_VERSION) {
    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetCypressVersionResponse(
            CypressFirmwareVersion.fromBytes(data),
        )
    }
}
