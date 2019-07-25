package com.simprints.fingerprintscannermock

import com.simprints.fingerprintscanner.Message
import com.simprints.fingerprintscannermock.ByteArrayUtils.byteArrayFromHexString
import com.simprints.fingerprintscannermock.ByteArrayUtils.bytesToMessage
import com.simprints.fingerprintscanner.enums.MESSAGE_TYPE.*


class MockResponseHelper(private val mockScannerManager: MockScannerManager) {

    fun createMockResponse(message: Message, finger: MockFinger): ByteArray =
            when (message.messageType) {
                SET_UI -> setUiResponse()
                UN20_WAKEUP -> un20WakeUpResponse()
                GET_SENSOR_INFO -> getSensorInfoResponse()
                ENABLE_FINGER_CHECK -> enableFingerCheckResponse()
                CAPTURE_IMAGE -> captureImageResponse()
                IMAGE_QUALITY -> imageQualityResponse(finger)
                GENERATE_TEMPLATE -> generateTemplateResponse(finger)
                GET_TEMPLATE_FRAGMENT -> getTemplateFragmentResponseAndCycleToNextFingerIfNeeded(message, finger)
                GET_RUNNING_BANK -> getRunningBankResponse()
                SET_RUNNING_BANK -> setRunningBankResponse()
                SET_OTA_META_DATA -> setOtaMetaData()
                SEND_OTA_DATA_PACKET -> sendOtaPacket()
                CRASH_FIRMWARE -> crashFirmware()
                else -> handleUnmockedResponse(message)
            }

    private fun setUiResponse() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 82 00 f5 f5 f5 f5 "
    )

    private fun un20WakeUpResponse() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 90 00 f5 f5 f5 f5 "
    )

    private fun getSensorInfoResponse() = byteArrayFromHexString(
            "fa fa fa fa 1d 00 80 00 b5 07 49 cc 88 06 06 00 06 00 a3 0e 83 0e 00 00 02 f5 f5 f5 f5 "
    )

    private fun enableFingerCheckResponse() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 9b 00 f5 f5 f5 f5 "
    )

    private fun captureImageResponse() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 85 00 f5 f5 f5 f5 "
    )

    private fun imageQualityResponse(finger: MockFinger) = byteArrayFromHexString(
            finger.imageQualityResponse
    )

    private fun generateTemplateResponse(finger: MockFinger) = byteArrayFromHexString(
            finger.generateTemplateResponse
    )

    private fun getTemplateFragmentResponseAndCycleToNextFingerIfNeeded(message: Message, finger: MockFinger): ByteArray {
        val response = byteArrayFromHexString(
                finger.getTemplateFragmentsResponses[message.fragmentNumber.toInt()]
        )
        if (bytesToMessage(response).isLastFragment) {
            mockScannerManager.cycleToNextFinger()
        }
        return response
    }

    private fun getRunningBankResponse() = byteArrayFromHexString(
            "fa fa fa fa 0d 00 9e 00 00 f5 f5 f5 f5 "
    )

    private fun setRunningBankResponse() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 80 01 f5 f5 f5 f5 " //this response = 'set bank request' rejected
    )

    private fun setOtaMetaData() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 9d 00 f5 f5 f5 f5 "
    )

    private fun sendOtaPacket() = byteArrayFromHexString(
            "fa fa fa fa 0c 00 9c 00 f5 f5 f5 f5 "
    )

    private fun crashFirmware() = byteArrayFromHexString("")

    private fun handleUnmockedResponse(message: Message): ByteArray {
        System.out.println("MockScannerManager : Unmocked message type: " + message.messageType.toString())
        return byteArrayOf()
    }
}
