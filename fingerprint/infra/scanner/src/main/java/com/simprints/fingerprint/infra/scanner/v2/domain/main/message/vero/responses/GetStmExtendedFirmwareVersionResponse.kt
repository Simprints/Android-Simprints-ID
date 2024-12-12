package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

data class GetStmExtendedFirmwareVersionResponse(
    val stmFirmwareVersion: StmExtendedFirmwareVersion,
) : VeroResponse(VeroMessageType.GET_STM_EXTENDED_FIRMWARE_VERSION) {
    override fun getDataBytes(): ByteArray = stmFirmwareVersion.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = GetStmExtendedFirmwareVersionResponse(StmExtendedFirmwareVersion.fromBytes(data))
    }
}
