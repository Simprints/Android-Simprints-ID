package com.simprints.fingerprintscanner.v2.domain.root.responses

import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.models.ExtendedHardwareVersion

class GetHardwareVersionResponse(val version: ExtendedHardwareVersion):
    RootResponse(RootMessageType.GET_HARDWARE_VERSION) {

    companion object {
        fun fromBytes(data: ByteArray) = GetHardwareVersionResponse(
            ExtendedHardwareVersion.fromBytes(data)
        )
    }
}
