package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.VeroMessageType

class GetBluetoothLedStateCommand : VeroCommand(VeroMessageType.GET_BLUETOOTH_LED_STATE) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetBluetoothLedStateCommand()
    }
}
