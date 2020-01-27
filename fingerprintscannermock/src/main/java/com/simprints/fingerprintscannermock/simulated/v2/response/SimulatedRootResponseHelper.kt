package com.simprints.fingerprintscannermock.simulated.v2.response

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.*
import com.simprints.fingerprintscanner.v2.domain.root.responses.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.v2.SimulatedScannerV2

class SimulatedRootResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<RootCommand, RootResponse> {

    override fun createResponseToCommand(command: RootCommand): RootResponse {
        val response = when (command) {
            is EnterMainModeCommand -> EnterMainModeResponse()
            is EnterCypressOtaModeCommand -> EnterCypressOtaModeResponse()
            is EnterStmOtaModeCommand -> EnterStmOtaModeResponse()
            is GetVersionCommand -> GetVersionResponse(simulatedScannerV2.scannerState.versionInfo)
            is SetVersionCommand -> SetVersionResponse()
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
