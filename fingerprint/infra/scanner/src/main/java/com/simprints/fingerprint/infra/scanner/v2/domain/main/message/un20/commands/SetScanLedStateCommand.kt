package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class SetScanLedStateCommand(
    val on: DigitalValue,
) : Un20Command(Un20MessageType.SetScanLedState) {
    override fun getDataBytes(): ByteArray = byteArrayOf(on.byte)

    companion object {
        fun fromBytes(data: ByteArray) = SetScanLedStateCommand(DigitalValue.fromBytes(data))
    }
}
