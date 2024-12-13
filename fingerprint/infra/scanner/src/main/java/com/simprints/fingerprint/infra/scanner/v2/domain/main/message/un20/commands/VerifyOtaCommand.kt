package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class VerifyOtaCommand(
    val crcCheck: Int,
) : Un20Command(Un20MessageType.VerifyOta) {
    override fun getDataBytes(): ByteArray = with(Un20MessageProtocol) { crcCheck.toByteArray() }

    companion object {
        fun fromBytes(data: ByteArray) = with(Un20MessageProtocol) {
            VerifyOtaCommand(data.extract({ int }))
        }
    }
}
