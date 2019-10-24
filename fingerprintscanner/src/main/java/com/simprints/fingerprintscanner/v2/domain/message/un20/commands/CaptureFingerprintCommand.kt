package com.simprints.fingerprintscanner.v2.domain.message.un20.commands

import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Dpi
import com.simprints.fingerprintscanner.v2.domain.message.un20.models.Un20MessageType

class CaptureFingerprintCommand(val dpi: Dpi) : Un20Command(Un20MessageType.CaptureFingerprint){

    override fun getDataBytes(): ByteArray = dpi.getBytes()
}
