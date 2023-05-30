package com.simprints.fingerprintscannermock.simulated.v2.response

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.*
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.*
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Calculator
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprintscannermock.simulated.v2.SimulatedFingerV2
import com.simprints.fingerprintscannermock.simulated.v2.SimulatedScannerV2
import com.simprints.fingerprintscannermock.simulated.v2.toV2
import kotlin.random.Random

class SimulatedUn20ResponseHelper(private val simulatedScannerManager: SimulatedScannerManager,
                                  private val simulatedScannerV2: SimulatedScannerV2) : SimulatedResponseHelperV2<Un20Command, Un20Response> {

    override fun createResponseToCommand(command: Un20Command): Un20Response {
        val response = when (command) {
            is GetUn20ExtendedAppVersionCommand -> GetUn20ExtendedAppVersionResponse(Un20ExtendedAppVersion("0.E-1.1"))
            is CaptureFingerprintCommand -> CaptureFingerprintResponse(
                simulatedScannerManager.currentMockFinger().toV2()
                    .also { if (it == SimulatedFingerV2.NO_FINGER) simulatedScannerManager.cycleToNextFinger() }
                    .captureFingerprintResult
            )
            is GetSupportedTemplateTypesCommand -> GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))
            is GetTemplateCommand -> GetTemplateResponse(command.templateType,
                TemplateData(
                    simulatedScannerManager.currentMockFinger().toV2().templateBytes
                )
            ).also { simulatedScannerManager.cycleToNextFinger() }
            is GetSupportedImageFormatsCommand -> GetSupportedImageFormatsResponse(setOf(ImageFormat.RAW))
            is GetImageCommand -> {
                val imageBytes = Random.nextBytes((120000 * (((simulatedScannerV2.scannerState.lastFingerCapturedDpi.value / 100) * 100) / 500f)).toInt())
                GetImageResponse(command.imageFormatData.imageFormat,
                    ImageData(
                        imageBytes,
                        Crc32Calculator().calculateCrc32(imageBytes)
                    )
                )
            }
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
