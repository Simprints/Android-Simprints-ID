package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Dpi
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class CaptureFingerprintCommand(
    val dpi: Dpi,
) : Un20Command(Un20MessageType.CaptureFingerprint) {
    override fun getDataBytes(): ByteArray = dpi.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = CaptureFingerprintCommand(Dpi.fromBytes(data))
    }
}
