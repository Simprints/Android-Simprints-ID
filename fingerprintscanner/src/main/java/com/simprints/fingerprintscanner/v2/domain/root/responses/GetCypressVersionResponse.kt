package com.simprints.fingerprintscanner.v2.domain.root.responses

import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.root.models.UnifiedVersionInformation

class GetCypressVersionResponse(val version: CypressFirmwareVersion) : RootResponse(RootMessageType.GET_CYPRESS_VERSION) {

    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetCypressVersionResponse(
            CypressFirmwareVersion.fromBytes(data)
        )
    }
}
