package com.simprints.fingerprint.infra.scannermock.simulated.v2.response


import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.CaptureFingerprintCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageDistortionConfigurationMatrixCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetImageQualityPreviewCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetSupportedImageFormatsCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetSupportedTemplateTypesCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetTemplateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUn20ExtendedAppVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUnprocessedImageCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.SetScanLedStateCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.ImageFormat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateData
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageDistortionConfigurationMatrixResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityPreviewResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageQualityResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetSupportedImageFormatsResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetSupportedTemplateTypesResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetTemplateResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetUn20ExtendedAppVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.SetScanLedStateResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.crc.Crc32Calculator
import com.simprints.fingerprint.infra.scannermock.R
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.common.RealisticSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulationSpeedBehaviour
import com.simprints.fingerprint.infra.scannermock.simulated.v2.SimulatedFingerV2
import com.simprints.fingerprint.infra.scannermock.simulated.v2.toV2

class SimulatedUn20ResponseHelper(
    private val simulatedScannerManager: SimulatedScannerManager
) : SimulatedResponseHelperV2<Un20Command, Un20Response> {

    override fun createResponseToCommand(command: Un20Command): Un20Response {
        val response = when (command) {
            is GetUn20ExtendedAppVersionCommand -> GetUn20ExtendedAppVersionResponse(
                Un20ExtendedAppVersion("0.E-1.1")
            )
            is CaptureFingerprintCommand -> CaptureFingerprintResponse(
                simulatedScannerManager.currentMockFinger().toV2()
                    .also { if (it == SimulatedFingerV2.NO_FINGER) simulatedScannerManager.cycleToNextFinger() }.captureFingerprintResult
            )
            is GetSupportedTemplateTypesCommand -> GetSupportedTemplateTypesResponse(
                setOf(
                    TemplateType.ISO_19794_2_2011
                )
            )
            is GetTemplateCommand -> GetTemplateResponse(
                command.templateType, TemplateData(
                    simulatedScannerManager.currentMockFinger().toV2().templateBytes
                )
            ).also { simulatedScannerManager.cycleToNextFinger() }
            is GetSupportedImageFormatsCommand -> GetSupportedImageFormatsResponse(
                setOf(
                    ImageFormat.RAW, ImageFormat.WSQ
                )
            )
            is GetImageCommand -> {
                val imageBytes = readFromResources(R.raw.corrected_fingerprint_image)
                GetImageResponse(
                    command.imageFormatData.imageFormat, ImageData(
                        imageBytes, Crc32Calculator().calculateCrc32(imageBytes)
                    )
                )
            }
            is GetImageQualityCommand -> GetImageQualityResponse(
                simulatedScannerManager.currentMockFinger().toV2().imageQuality
            )
            is SetScanLedStateCommand -> SetScanLedStateResponse(OperationResultCode.OK)
            is GetImageQualityPreviewCommand -> GetImageQualityPreviewResponse(70)
            is GetImageDistortionConfigurationMatrixCommand -> GetImageDistortionConfigurationMatrixResponse(
                readFromResources(R.raw.image_distortion_config)
            )

            is GetUnprocessedImageCommand -> {
                val imageBytes = readFromResources(R.raw.raw_image)
                GetImageResponse(ImageFormat.WSQ,ImageData(imageBytes,1))
            }

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

    private fun readFromResources(resId: Int): ByteArray {
        simulatedScannerManager.context?.resources?.openRawResource(resId)?.use { inputStream ->
            return inputStream.readBytes()
        }
        return byteArrayOf(0)
    }
}
