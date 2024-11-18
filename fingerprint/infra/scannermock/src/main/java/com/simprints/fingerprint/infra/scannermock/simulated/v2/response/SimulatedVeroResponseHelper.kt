package com.simprints.fingerprint.infra.scannermock.simulated.v2.response

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryCurrentCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryPercentChargeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryTemperatureCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBatteryVoltageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetBluetoothLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetPowerLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetSmileLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetStmExtendedFirmwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetTriggerButtonActiveCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetTriggerButtonActiveCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryCurrent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryPercentCharge
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryTemperature
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.BatteryVoltage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.LedState
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryCurrentResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryPercentChargeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryTemperatureResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBatteryVoltageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetBluetoothLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetPowerLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetSmileLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetStmExtendedFirmwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetTriggerButtonActiveResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetSmileLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetTriggerButtonActiveResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.v2.SimulatedScannerV2

class SimulatedVeroResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<VeroCommand, VeroResponse> {

    override fun createResponseToCommand(command: VeroCommand): VeroResponse {
        val response = when (command) {
            is GetStmExtendedFirmwareVersionCommand -> GetStmExtendedFirmwareVersionResponse(StmExtendedFirmwareVersion("0.E-1.1"))
            is GetUn20OnCommand -> GetUn20OnResponse(if (simulatedScannerV2.scannerState.isUn20On) DigitalValue.TRUE else DigitalValue.FALSE)
            is SetUn20OnCommand -> SetUn20OnResponse(OperationResultCode.OK)
            is GetTriggerButtonActiveCommand -> GetTriggerButtonActiveResponse(
                if (simulatedScannerV2.scannerState.isTriggerButtonActive) DigitalValue.TRUE else DigitalValue.FALSE
            )
            is SetTriggerButtonActiveCommand -> SetTriggerButtonActiveResponse(OperationResultCode.OK)
            is GetSmileLedStateCommand -> GetSmileLedStateResponse(simulatedScannerV2.scannerState.smileLedState)
            is GetBluetoothLedStateCommand -> GetBluetoothLedStateResponse(
                LedState(DigitalValue.FALSE, 0x00, 0x00, 0xFF.toByte())
            )
            is GetPowerLedStateCommand -> GetPowerLedStateResponse(
                LedState(DigitalValue.FALSE, 0x00, 0xFF.toByte(), 0x00)
            )
            is SetSmileLedStateCommand -> SetSmileLedStateResponse(OperationResultCode.OK)
            is GetBatteryPercentChargeCommand -> GetBatteryPercentChargeResponse(BatteryPercentCharge(simulatedScannerV2.scannerState.batteryPercentCharge.toByte()))
            is GetBatteryVoltageCommand -> GetBatteryVoltageResponse(BatteryVoltage(simulatedScannerV2.scannerState.batteryVoltageMilliVolts.toShort()))
            is GetBatteryCurrentCommand -> GetBatteryCurrentResponse(BatteryCurrent(simulatedScannerV2.scannerState.batteryCurrentMilliAmps.toShort()))
            is GetBatteryTemperatureCommand -> GetBatteryTemperatureResponse(BatteryTemperature(simulatedScannerV2.scannerState.batteryTemperatureDeciKelvin.toShort()))
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
