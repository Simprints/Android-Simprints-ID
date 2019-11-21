package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.FirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetFirmwareVersionResponse(val firmwareVersion: FirmwareVersion) : VeroResponse(VeroMessageType.GET_FIRMWARE_VERSION) {

    override fun getDataBytes(): ByteArray = firmwareVersion.getBytes()

        companion object {
        fun fromBytes(data: ByteArray) =
            GetFirmwareVersionResponse(FirmwareVersion.fromBytes(data))
    }
}
