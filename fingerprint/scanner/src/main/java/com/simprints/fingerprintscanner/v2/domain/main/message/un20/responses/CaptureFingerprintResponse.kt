package com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Response
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.CaptureFingerprintResult
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20MessageType

class CaptureFingerprintResponse(val captureFingerprintResult: CaptureFingerprintResult) : Un20Response(Un20MessageType.CaptureFingerprint) {

    override fun getDataBytes(): ByteArray = byteArrayOf(captureFingerprintResult.byte)

    companion object {
        fun fromBytes(data: ByteArray) =
            CaptureFingerprintResponse(CaptureFingerprintResult.fromBytes(data))
    }
}
