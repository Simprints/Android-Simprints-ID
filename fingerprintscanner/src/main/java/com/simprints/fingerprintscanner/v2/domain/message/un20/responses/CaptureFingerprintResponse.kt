package com.simprints.fingerprintscanner.v2.domain.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class CaptureFingerprintResponse(val responseCode: ResponseCode) : Un20Response(Un20MessageType.CaptureFingerprint) {

    enum class ResponseCode(val byte: Byte) {
        OK(0x00),
        FINGERPRINT_NOT_FOUND(0x20),
        DPI_UNSUPPORTED(0x21),
        UNKNOWN_ERROR(0xFF.toByte());

        companion object {
            fun fromBytes(bytes: ByteArray) = values().find { it.byte == bytes[0] } ?: TODO("exception handling")
        }
    }

    companion object {
        fun fromBytes(data: ByteArray) =
            CaptureFingerprintResponse(ResponseCode.fromBytes(data))
    }
}
