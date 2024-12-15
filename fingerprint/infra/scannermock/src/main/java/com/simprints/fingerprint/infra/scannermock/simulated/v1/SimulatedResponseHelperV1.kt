package com.simprints.fingerprint.infra.scannermock.simulated.v1

import com.simprints.fingerprint.infra.scanner.v1.Message
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.CAPTURE_IMAGE
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.CRASH_FIRMWARE
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.ENABLE_FINGER_CHECK
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.GENERATE_TEMPLATE
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.GET_RUNNING_BANK
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.GET_SENSOR_INFO
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.GET_TEMPLATE_FRAGMENT
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.IMAGE_QUALITY
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.SEND_OTA_DATA_PACKET
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.SET_OTA_META_DATA
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.SET_RUNNING_BANK
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.SET_UI
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.UN20_SHUTDOWN
import com.simprints.fingerprint.infra.scanner.v1.enums.MESSAGE_TYPE.UN20_WAKEUP
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.tools.byteArrayFromHexString

class SimulatedResponseHelperV1(
    private val simulatedScannerManager: SimulatedScannerManager,
    private val simulatedScanner: SimulatedScannerV1,
) {
    fun createMockResponse(message: Message): ByteArray = when (message.messageType) {
        SET_UI -> setUiResponse()
        UN20_WAKEUP -> un20WakeUpResponse()
        GET_SENSOR_INFO -> getSensorInfoResponse()
        ENABLE_FINGER_CHECK -> enableFingerCheckResponse()
        CAPTURE_IMAGE -> captureImageResponse()
        IMAGE_QUALITY -> imageQualityResponse()
        GENERATE_TEMPLATE -> generateTemplateResponse()
        GET_TEMPLATE_FRAGMENT -> getTemplateFragmentResponseAndCycleToNextFingerIfNeeded(message)
        UN20_SHUTDOWN -> un20ShutdownResponse()
        GET_RUNNING_BANK -> getRunningBankResponse()
        SET_RUNNING_BANK -> setRunningBankResponse()
        SET_OTA_META_DATA -> setOtaMetaData()
        SEND_OTA_DATA_PACKET -> sendOtaPacket()
        CRASH_FIRMWARE -> crashFirmware()
        else -> handleUnmockedResponse(message)
    }

    private fun setUiResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 82 00 f5 f5 f5 f5 ",
    )

    private fun un20WakeUpResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 90 00 f5 f5 f5 f5 ",
    )

    private fun getSensorInfoResponse() = if (simulatedScanner.scannerState.isUn20On) {
        byteArrayFromHexString(
            "fa fa fa fa 1d 00 80 00 b5 07 49 cc 88 06 06 00 06 00 a3 0e 83 0e 00 00 02 f5 f5 f5 f5 ",
        )
    } else {
        byteArrayFromHexString(
            "fa fa fa fa 1d 00 80 00 b5 07 49 cc 88 06 06 00 06 00 a3 0e 83 0e 00 00 00 f5 f5 f5 f5 ",
        )
    }

    private fun enableFingerCheckResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 9b 00 f5 f5 f5 f5 ",
    )

    private fun captureImageResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 85 00 f5 f5 f5 f5 ",
    )

    private fun imageQualityResponse() = byteArrayFromHexString(
        simulatedScannerManager.currentMockFinger().toV1().imageQualityResponse,
    )

    private fun generateTemplateResponse() = byteArrayFromHexString(
        simulatedScannerManager.currentMockFinger().toV1().generateTemplateResponse,
    )

    private fun getTemplateFragmentResponseAndCycleToNextFingerIfNeeded(message: Message): ByteArray {
        val response = byteArrayFromHexString(
            simulatedScannerManager.currentMockFinger().toV1().getTemplateFragmentsResponses[message.fragmentNumber.toInt()],
        )
        if (response.toMessageV1().isLastFragment) {
            simulatedScannerManager.cycleToNextFinger()
        }
        return response
    }

    private fun un20ShutdownResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 8f 00 f5 f5 f5 f5 ",
    )

    private fun getRunningBankResponse() = byteArrayFromHexString(
        "fa fa fa fa 0d 00 9e 00 00 f5 f5 f5 f5 ",
    )

    private fun setRunningBankResponse() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 80 01 f5 f5 f5 f5 ", // this response = 'set bank request' rejected
    )

    private fun setOtaMetaData() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 9d 00 f5 f5 f5 f5 ",
    )

    private fun sendOtaPacket() = byteArrayFromHexString(
        "fa fa fa fa 0c 00 9c 00 f5 f5 f5 f5 ",
    )

    private fun crashFirmware() = byteArrayFromHexString("")

    private fun handleUnmockedResponse(message: Message): ByteArray =
        throw UnsupportedOperationException("SimulatedScannerManager : Unmocked message type: $message.messageType.toString()")
}
