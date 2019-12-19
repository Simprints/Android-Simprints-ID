package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20AppVersion
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class GetUn20AppVersionResponse(val un20AppVersion: Un20AppVersion) : Un20Response(Un20MessageType.GetUn20AppVersion) {

    override fun getDataBytes(): ByteArray = un20AppVersion.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) =
            GetUn20AppVersionResponse(Un20AppVersion.fromBytes(data))
    }
}
