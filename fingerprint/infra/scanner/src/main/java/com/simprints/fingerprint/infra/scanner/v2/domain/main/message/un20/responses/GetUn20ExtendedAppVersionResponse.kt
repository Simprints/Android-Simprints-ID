package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

data class GetUn20ExtendedAppVersionResponse(
    val un20AppVersion: Un20ExtendedAppVersion,
) : Un20Response(Un20MessageType.GetUn20ExtendedAppVersion) {
    override fun getDataBytes(): ByteArray = un20AppVersion.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetUn20ExtendedAppVersionResponse(Un20ExtendedAppVersion.fromBytes(data))
    }
}
