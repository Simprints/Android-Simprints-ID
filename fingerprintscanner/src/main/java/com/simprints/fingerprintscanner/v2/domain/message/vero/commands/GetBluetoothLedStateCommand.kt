package com.simprints.fingerprintscanner.v2.domain.message.vero.commands

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.VeroMessageType

class GetBluetoothLedStateCommand : VeroCommand(VeroMessageType.GET_BLUETOOTH_LED_STATE)
