package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour

class SimulatedVeroResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<VeroCommand, VeroResponse> {

    override fun createResponseToCommand(command: VeroCommand): VeroResponse {
        val response = when (command) {
            is GetStmFirmwareVersionCommand -> GetStmFirmwareVersionResponse(StmFirmwareVersion(0.toShort(), 1.toShort(), 6.toShort(), 0.toShort()))
            is GetUn20OnCommand -> GetUn20OnResponse(if (simulatedScannerV2.scannerState.isUn20On) DigitalValue.TRUE else DigitalValue.FALSE)
            is SetUn20OnCommand -> SetUn20OnResponse(OperationResultCode.OK)
            is GetTriggerButtonActiveCommand -> GetTriggerButtonActiveResponse(DigitalValue.TRUE)
            is SetTriggerButtonActiveCommand -> SetTriggerButtonActiveResponse(OperationResultCode.OK)
            is GetSmileLedStateCommand -> GetSmileLedStateResponse(SmileLedState(
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00),
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)))
            is GetBluetoothLedStateCommand -> GetBluetoothLedStateResponse(
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            is GetPowerLedStateCommand -> GetPowerLedStateResponse(
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0x00)
            )
            is SetSmileLedStateCommand -> SetSmileLedStateResponse(OperationResultCode.OK)
            is GetBatteryPercentChargeCommand -> GetBatteryPercentChargeResponse(BatteryPercentCharge(80.toShort()))
            else -> throw UnsupportedOperationException("Un-mocked response to $command in SimulatedVeroResponseHelper")
        }

        val delay = when (simulatedScannerManager.simulationSpeedBehaviour) {
            SimulationSpeedBehaviour.INSTANT -> 0L
            SimulationSpeedBehaviour.REALISTIC -> RealisticSpeedBehaviour.DEFAULT_RESPONSE_DELAY_MS
        }

        Thread.sleep(delay)

        return response
    }
}
