package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroResponse
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.*
import com.simprints.fingerprintscanner.v2.domain.message.vero.responses.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager

class SimulatedVeroResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<VeroCommand, VeroResponse> {

    override fun createResponseToCommand(command: VeroCommand): VeroResponse =
        when (command) {
            is GetFirmwareVersionCommand -> GetFirmwareVersionResponse(FirmwareVersion(2.toShort(), 0.toShort(), 6, byteArrayOf(0x12, 0x34, 0x56, 0x78)))
            is GetUn20OnCommand -> GetUn20OnResponse(if (simulatedScannerV2.scannerState.isUn20On) DigitalValue.TRUE else DigitalValue.FALSE)
            is SetUn20OnCommand -> SetUn20OnResponse(OperationResultCode.OK)
            is GetTriggerButtonActiveCommand -> GetTriggerButtonActiveResponse(DigitalValue.TRUE)
            is SetTriggerButtonActiveCommand -> SetTriggerButtonActiveResponse(OperationResultCode.OK)
            is GetSmileLedStateCommand -> GetSmileLedStateResponse(SmileLedState(
                LedState(LedMode.OFF, 0x00, 0x00, 0x00),
                LedState(LedMode.OFF, 0x00, 0x00, 0x00),
                LedState(LedMode.OFF, 0x00, 0x00, 0x00),
                LedState(LedMode.OFF, 0x00, 0x00, 0x00),
                LedState(LedMode.OFF, 0x00, 0x00, 0x00)))
            is GetBluetoothLedStateCommand -> GetBluetoothLedStateResponse(
                LedState(LedMode.OFF, 0x00, 0x00, 0x00)
            )
            is GetPowerLedStateCommand -> GetPowerLedStateResponse(
                LedState(LedMode.OFF, 0x00, 0x00, 0x00)
            )
            is SetSmileLedStateCommand -> SetSmileLedStateResponse(OperationResultCode.OK)
            is SetBluetoothLedStateCommand -> SetBluetoothLedStateResponse(OperationResultCode.OK)
            is SetPowerLedStateCommand -> SetPowerLedStateResponse(OperationResultCode.OK)
            else -> throw UnsupportedOperationException("Un-mocked response to $command in SimulatedVeroResponseHelper")
        }
}
