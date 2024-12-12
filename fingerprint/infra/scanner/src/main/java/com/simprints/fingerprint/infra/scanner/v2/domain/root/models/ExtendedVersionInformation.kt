package com.simprints.fingerprint.infra.scanner.v2.domain.root.models

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion

/**
 * This class represents the version info read from the new/extended firmware api, it holds the
 * version information for all chip's firmware in its respective fields.
 */
data class ExtendedVersionInformation(
    val cypressFirmwareVersion: CypressExtendedFirmwareVersion,
    val stmFirmwareVersion: StmExtendedFirmwareVersion,
    val un20AppVersion: Un20ExtendedAppVersion,
) {
    fun getBytes() = com.simprints.fingerprint.infra.scanner.v2.tools.primitives.byteArrayOf(
        cypressFirmwareVersion.getBytes(),
        stmFirmwareVersion.getBytes(),
        un20AppVersion.getBytes(),
    )

    companion object {
        fun fromBytes(bytes: ByteArray): ExtendedVersionInformation {
            val extractor = ByteExtractor(bytes)

            if (bytes.isEmpty()) {
                return ExtendedVersionInformation(
                    cypressFirmwareVersion = CypressExtendedFirmwareVersion.fromString(""),
                    stmFirmwareVersion = StmExtendedFirmwareVersion.fromString(""),
                    un20AppVersion = Un20ExtendedAppVersion.fromString(""),
                )
            }

            val cypressVersionString = extractor.nextData()
            val stmVersionString = extractor.nextData()
            val un20VersionString = extractor.nextData()

            return ExtendedVersionInformation(
                cypressFirmwareVersion = CypressExtendedFirmwareVersion.fromString(cypressVersionString),
                stmFirmwareVersion = StmExtendedFirmwareVersion.fromString(stmVersionString),
                un20AppVersion = Un20ExtendedAppVersion.fromString(un20VersionString),
            )
        }

        const val UNKNOWN_FIRMWARE_VERSION = ""

        val UNKNOWN = ExtendedVersionInformation(
            cypressFirmwareVersion = CypressExtendedFirmwareVersion(UNKNOWN_FIRMWARE_VERSION),
            stmFirmwareVersion = StmExtendedFirmwareVersion(UNKNOWN_FIRMWARE_VERSION),
            un20AppVersion = Un20ExtendedAppVersion(UNKNOWN_FIRMWARE_VERSION),
        )
    }

    /**
     * This is a helper class that extracts the firmware's versionInfo as a string, which is packed
     * in a [bytes] ByteArray, in the form of: [dataLength, data, nextDataLength, nextData, ...]
     */
    private class ByteExtractor(
        private val bytes: ByteArray,
    ) {
        private var dataLength = 0
        private var startPosition = -1
        private var endPosition = -1

        fun nextData(): String {
            dataLength = bytes[endPosition + 1].toInt()
            startPosition = endPosition + 2
            endPosition = startPosition + dataLength - 1

            return String(bytes.sliceArray(startPosition..endPosition))
        }
    }
}
