package com.simprints.fingerprintscanner.v2.domain.message.vero.responses

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetStmFirmwareVersionResponse(val stmFirmwareVersion: StmFirmwareVersion) : VeroResponse(VeroMessageType.GET_STM_FIRMWARE_VERSION) {

    override fun getDataBytes(): ByteArray = stmFirmwareVersion.getBytes()

        companion object {
        fun fromBytes(data: ByteArray) =
            GetStmFirmwareVersionResponse(StmFirmwareVersion.fromBytes(data))
    }
}
