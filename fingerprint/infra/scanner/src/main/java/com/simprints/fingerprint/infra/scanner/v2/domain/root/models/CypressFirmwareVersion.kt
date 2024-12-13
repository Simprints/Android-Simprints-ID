package com.simprints.fingerprint.infra.scanner.v2.domain.root.models

import com.simprints.fingerprint.infra.scanner.v2.domain.FirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toByteArray

class CypressFirmwareVersion(
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
    fun getBytes() = with(RootMessageProtocol) {
        apiMajorVersion.toByteArray(byteOrder) +
            apiMinorVersion.toByteArray(byteOrder) +
            firmwareMajorVersion.toByteArray(byteOrder) +
            firmwareMinorVersion.toByteArray(byteOrder)
    }

    companion object {
        fun fromBytes(bytes: ByteArray) = with(RootMessageProtocol) {
            CypressFirmwareVersion(
                apiMajorVersion = bytes.extract({ short }, 0..1),
                apiMinorVersion = bytes.extract({ short }, 2..3),
                firmwareMajorVersion = bytes.extract({ short }, 4..5),
                firmwareMinorVersion = bytes.extract({ short }, 6..7),
            )
        }
    }
}
