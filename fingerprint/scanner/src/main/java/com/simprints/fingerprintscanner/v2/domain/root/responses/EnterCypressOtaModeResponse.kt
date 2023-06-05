package com.simprints.fingerprintscanner.v2.domain.root.responses

import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse

class EnterCypressOtaModeResponse : RootResponse(RootMessageType.ENTER_CYPRESS_OTA_MODE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = EnterCypressOtaModeResponse()
    }
}
