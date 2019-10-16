package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.commands.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager

class SimulatedUn20ResponseHelper(simulatedScannerManager: SimulatedScannerManager,
                                  simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<Un20Command, Un20Response> {

    override fun createResponseToCommand(command: Un20Command): Un20Response =
        when(command) {
            is GetUn20AppVersionCommand -> TODO()
            is CaptureFingerprintCommand -> TODO()
            is GetSupportedTemplateTypesCommand -> TODO()
            is GetTemplateCommand -> TODO()
            is GetSupportedImageFormatsCommand -> TODO()
            is GetImageCommand -> TODO()
            else -> TODO()
        }
}
