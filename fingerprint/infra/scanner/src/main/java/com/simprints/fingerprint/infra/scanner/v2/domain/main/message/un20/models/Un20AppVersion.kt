package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models

import com.simprints.fingerprint.infra.scanner.v2.domain.FirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20MessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toByteArray

@Deprecated(message = "Use new extension api: [Un20ExtendedAppVersion]")
class Un20AppVersion(
    apiMajorVersion: Short,
    apiMinorVersion: Short,
    firmwareMajorVersion: Short,
    firmwareMinorVersion: Short,
) : FirmwareVersion(
        apiMajorVersion,
        apiMinorVersion,
        firmwareMajorVersion,
        firmwareMinorVersion,
    ) {
    fun getBytes() = with(Un20MessageProtocol) {
        apiMajorVersion.toByteArray(byteOrder) +
            apiMinorVersion.toByteArray(byteOrder) +
            firmwareMajorVersion.toByteArray(byteOrder) +
            firmwareMinorVersion.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) = with(Un20MessageProtocol) {
            Un20AppVersion(
                apiMajorVersion = bytes.extract({ short }, 0..1),
                apiMinorVersion = bytes.extract({ short }, 2..3),
                firmwareMajorVersion = bytes.extract({ short }, 4..5),
                firmwareMinorVersion = bytes.extract({ short }, 6..7),
            )
        }
    }
}
