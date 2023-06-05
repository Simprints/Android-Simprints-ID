package com.simprints.fingerprintscanner.v2.domain.root.responses

import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse

class SetVersionResponse : RootResponse(RootMessageType.SET_VERSION) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = SetVersionResponse()
    }
}
