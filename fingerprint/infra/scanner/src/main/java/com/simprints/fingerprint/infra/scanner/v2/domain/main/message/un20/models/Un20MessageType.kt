package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models

import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprint.infra.scanner.v2.tools.lang.objects
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toHexString

sealed class Un20MessageType(
    val majorByte: Byte,
    val minorByte: Byte,
) {
    // 0x1_ : Versioning & OTA
    data object GetUn20ExtendedAppVersion : Un20MessageType(Un20MessageMajorType.GET_UN20_EXTENDED_APP_VERSION.majorByte, 0X01)

    data object StartOta : Un20MessageType(Un20MessageMajorType.START_OTA.majorByte, IGNORED)

    data object WriteOtaChunk : Un20MessageType(Un20MessageMajorType.WRITE_OTA_CHUNK.majorByte, IGNORED)

    data object VerifyOta : Un20MessageType(Un20MessageMajorType.VERIFY_OTA.majorByte, IGNORED)

    // 0x2_ : Sensor commands
    data object CaptureFingerprint : Un20MessageType(Un20MessageMajorType.CAPTURE_FINGERPRINT.majorByte, IGNORED)

    data object GetImageQualityPreview : Un20MessageType(Un20MessageMajorType.GET_IMAGE_QUALITY_PREVIEW.majorByte, IGNORED)

    data object SetScanLedState : Un20MessageType(Un20MessageMajorType.SET_SCAN_LED_STATE.majorByte, IGNORED)

    // 0x3_ : Template commands
    data object GetSupportedTemplateTypes : Un20MessageType(Un20MessageMajorType.GET_SUPPORTED_TEMPLATE_TYPES.majorByte, IGNORED)

    class GetTemplate(
        minorByte: Byte,
    ) : Un20MessageType(Un20MessageMajorType.GET_TEMPLATE.majorByte, minorByte)

    // 0x4_ : Image commands
    data object GetSupportedImageFormats : Un20MessageType(Un20MessageMajorType.GET_SUPPORTED_IMAGE_FORMATS.majorByte, IGNORED)

    class GetImage(
        minorByte: Byte,
    ) : Un20MessageType(Un20MessageMajorType.GET_IMAGE.majorByte, minorByte)

    data object GetImageQuality : Un20MessageType(Un20MessageMajorType.GET_IMAGE_QUALITY.majorByte, IGNORED)

    class GetUnprocessedImage(
        minorByte: Byte,
    ) : Un20MessageType(Un20MessageMajorType.GET_UNPROCESSED_IMAGE.majorByte, minorByte)

    data object GetImageDistortionConfigurationMatrix : Un20MessageType(
        Un20MessageMajorType.GET_IMAGE_DISTORTION_CONFIGURATION_MATRIX.majorByte,
        IGNORED,
    )

    fun getBytes() = byteArrayOf(majorByte, minorByte)

    companion object {
        const val IGNORED = 0x00.toByte()

        fun fromBytes(bytes: ByteArray): Un20MessageType = Pair(bytes[0], bytes[1]).let { (receivedMajorByte, receivedMinorByte) ->

            Un20MessageType::class
                .objects()
                .firstOrNull {
                    it.majorByte == receivedMajorByte && it.minorByte == receivedMinorByte
                }
                ?: when (receivedMajorByte) {
                    Un20MessageMajorType.GET_TEMPLATE.majorByte -> GetTemplate(receivedMinorByte)
                    Un20MessageMajorType.GET_IMAGE.majorByte -> GetImage(receivedMinorByte)
                    Un20MessageMajorType.GET_UNPROCESSED_IMAGE.majorByte -> GetUnprocessedImage(receivedMinorByte)
                    else -> throw InvalidMessageException("Invalid Un20MessageType received with bytes: ${bytes.toHexString()}")
                }
        }
    }
}
