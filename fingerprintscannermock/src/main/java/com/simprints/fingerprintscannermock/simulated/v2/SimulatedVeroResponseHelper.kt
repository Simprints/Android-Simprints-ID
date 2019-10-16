package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager

class SimulatedVeroResponseHelper(simulatedScannerManager: SimulatedScannerManager,
                                  simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<VeroCommand> {

    override fun createResponseToCommand(command: VeroCommand): ByteArray =
        when(command) {
            is GetFirmwareVersionCommand -> TODO()
            is GetUn20OnCommand -> TODO()
            is SetUn20OnCommand -> TODO()
            is GetTriggerButtonActiveCommand -> TODO()
            is SetTriggerButtonActiveCommand -> TODO()
            is GetSmileLedStateCommand -> TODO()
            is GetBluetoothLedStateCommand -> TODO()
            is GetPowerLedStateCommand -> TODO()
            is SetSmileLedStateCommand -> TODO()
            is SetBluetoothLedStateCommand -> TODO()
            is SetPowerLedStateCommand -> TODO()
            else -> TODO()
        }
}
