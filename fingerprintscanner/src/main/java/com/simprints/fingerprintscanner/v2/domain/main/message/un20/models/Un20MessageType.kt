package com.simprints.fingerprintscanner.v2.domain.main.message.un20.models

import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.fingerprintscanner.v2.tools.lang.objects
import com.simprints.fingerprintscanner.v2.tools.primitives.toHexString

sealed class Un20MessageType(val majorByte: Byte, val minorByte: Byte) {

    // 0x1_ : Versioning & OTA
    object GetUn20AppVersion : Un20MessageType(Un20MessageMajorType.GET_UN20_APP_VERSION.majorByte, IGNORED)

    object StartOta : Un20MessageType(Un20MessageMajorType.START_OTA.majorByte, IGNORED)

    object WriteOtaChunk : Un20MessageType(Un20MessageMajorType.WRITE_OTA_CHUNK.majorByte, IGNORED)

    object VerifyOta : Un20MessageType(Un20MessageMajorType.VERIFY_OTA.majorByte, IGNORED)

    // 0x2_ : Sensor commands
    object CaptureFingerprint : Un20MessageType(Un20MessageMajorType.CAPTURE_FINGERPRINT.majorByte, IGNORED)

    // 0x3_ : Template commands
    object GetSupportedTemplateTypes : Un20MessageType(Un20MessageMajorType.GET_SUPPORTED_TEMPLATE_TYPES.majorByte, IGNORED)

    class GetTemplate(minorByte: Byte) : Un20MessageType(Un20MessageMajorType.GET_TEMPLATE.majorByte, minorByte)

    // 0x4_ : Image commands
    object GetSupportedImageFormats : Un20MessageType(Un20MessageMajorType.GET_SUPPORTED_IMAGE_FORMATS.majorByte, IGNORED)

    class GetImage(minorByte: Byte) : Un20MessageType(Un20MessageMajorType.GET_IMAGE.majorByte, minorByte)

    object GetImageQuality : Un20MessageType(Un20MessageMajorType.GET_IMAGE_QUALITY.majorByte, IGNORED)

    fun getBytes() = byteArrayOf(majorByte, minorByte)

    companion object {
        const val IGNORED = 0x00.toByte()

        fun fromBytes(bytes: ByteArray): Un20MessageType =
            Pair(bytes[0], bytes[1]).let { (receivedMajorByte, receivedMinorByte) ->

                Un20MessageType::class.objects()
                    .firstOrNull {
                        it.majorByte == receivedMajorByte && it.minorByte == receivedMinorByte
                    }
                    ?: when (receivedMajorByte) {
                        Un20MessageMajorType.GET_TEMPLATE.majorByte -> GetTemplate(receivedMinorByte)
                        Un20MessageMajorType.GET_IMAGE.majorByte -> GetImage(receivedMinorByte)
                        else -> throw InvalidMessageException("Invalid Un20MessageType received with bytes: ${bytes.toHexString()}")
                    }
            }
    }
}
