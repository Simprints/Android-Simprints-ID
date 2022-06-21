package com.simprints.fingerprintscanner.v2.domain.main.message.vero.models

import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.tools.primitives.toByteArray

@Deprecated(message = "Use new extension api: [StmExtendedFirmwareVersion]")
class StmFirmwareVersion(
    val apiMajorVersion: Short,
    val apiMinorVersion: Short,
    val firmwareMajorVersion: Short,
    val firmwareMinorVersion: Short
) {

    fun getBytes() = with(VeroMessageProtocol) {
        apiMajorVersion.toByteArray(byteOrder) +
            apiMinorVersion.toByteArray(byteOrder) +
            firmwareMajorVersion.toByteArray(byteOrder) +
            firmwareMinorVersion.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) =
            with(VeroMessageProtocol) {
                StmFirmwareVersion(
                    apiMajorVersion = bytes.extract({ short }, 0..1),
                    apiMinorVersion = bytes.extract({ short }, 2..3),
                    firmwareMajorVersion = bytes.extract({ short }, 4..5),
                    firmwareMinorVersion = bytes.extract({ short }, 6..7)
                )
            }
    }
}
