package com.simprints.fingerprint.infra.scannermock.simulated.v2.response

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.SetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterCypressOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterStmOtaModeResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.SetVersionResponse
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.v2.SimulatedScannerV2

class SimulatedRootResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<RootCommand, RootResponse> {

    override fun createResponseToCommand(command: RootCommand): RootResponse {
        val response = when (command) {
            is EnterMainModeCommand -> EnterMainModeResponse()
            is EnterCypressOtaModeCommand -> EnterCypressOtaModeResponse()
            is EnterStmOtaModeCommand -> EnterStmOtaModeResponse()
            is GetVersionCommand -> GetVersionResponse(simulatedScannerV2.scannerState.versionInfo)
            is SetVersionCommand -> SetVersionResponse()
            is GetCypressVersionCommand -> GetCypressVersionResponse(CypressFirmwareVersion(1,0,0,0))
            else -> throw UnsupportedOperationException("Un-mocked response to $command in SimulatedRootResponseHelper")
        }

        val delay = when (simulatedScannerManager.simulationSpeedBehaviour) {
            SimulationSpeedBehaviour.INSTANT -> 0L
            SimulationSpeedBehaviour.REALISTIC -> RealisticSpeedBehaviour.DEFAULT_RESPONSE_DELAY_MS
        }

        Thread.sleep(delay)

        return response
    }
}
