package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.*
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour
import kotlin.random.Random

class SimulatedUn20ResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<Un20Command, Un20Response> {

    override fun createResponseToCommand(command: Un20Command): Un20Response {
        val response = when (command) {
            is GetUn20AppVersionCommand -> GetUn20AppVersionResponse(Un20AppVersion(0.toShort(), 1.toShort(), 6.toShort(), 0.toShort()))
            is CaptureFingerprintCommand -> CaptureFingerprintResponse(
                simulatedScannerManager.currentMockFinger().toV2()
                    .also { if (it == SimulatedFingerV2.NO_FINGER) simulatedScannerManager.cycleToNextFinger() }
                    .captureFingerprintResult
            )
            is GetSupportedTemplateTypesCommand -> GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))
            is GetTemplateCommand -> GetTemplateResponse(TemplateData(
                command.templateType,
                simulatedScannerManager.currentMockFinger().toV2().templateBytes
            )
                .also { simulatedScannerManager.cycleToNextFinger() })
            is GetSupportedImageFormatsCommand -> GetSupportedImageFormatsResponse(setOf(ImageFormat.RAW))
            is GetImageCommand -> GetImageResponse(ImageData(command.imageFormat, Random.nextBytes(120000), Random.nextBytes(2))) // TODO
            is GetImageQualityCommand -> GetImageQualityResponse(simulatedScannerManager.currentMockFinger().toV2().imageQuality)
            else -> throw UnsupportedOperationException("Unmocked response to $command in SimulatedUn20ResponseHelper")
        }

        val delay = when (simulatedScannerManager.simulationSpeedBehaviour) {
            SimulationSpeedBehaviour.INSTANT -> 0L
            SimulationSpeedBehaviour.REALISTIC -> {
                when (response) {
                    is CaptureFingerprintResponse -> RealisticSpeedBehaviour.CAPTURE_FINGERPRINT_DELAY_MS
                    else -> RealisticSpeedBehaviour.DEFAULT_RESPONSE_DELAY_MS
                }
            }
        }

        Thread.sleep(delay)

        return response
    }
}
