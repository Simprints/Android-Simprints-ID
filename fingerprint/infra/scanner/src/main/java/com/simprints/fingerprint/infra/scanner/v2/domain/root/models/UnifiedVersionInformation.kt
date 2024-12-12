package com.simprints.fingerprint.infra.scanner.v2.domain.root.models

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20AppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageProtocol
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.byteArrayOf
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.toByteArray

class UnifiedVersionInformation(
    val masterFirmwareVersion: Long,
    val cypressFirmwareVersion: CypressFirmwareVersion,
    val stmFirmwareVersion: StmFirmwareVersion,
    val un20AppVersion: Un20AppVersion,
) {
    fun getBytes() = with(RootMessageProtocol) {
        byteArrayOf(
            masterFirmwareVersion.toByteArray(byteOrder),
            cypressFirmwareVersion.getBytes(),
            stmFirmwareVersion.getBytes(),
            un20AppVersion.getBytes(),
        )
    }

    fun toExtendedVersionInfo() = ExtendedVersionInformation(
        cypressFirmwareVersion = CypressExtendedFirmwareVersion(
            versionAsString = cypressFirmwareVersion.toNewVersionNamingScheme(),
        ),
        stmFirmwareVersion = StmExtendedFirmwareVersion(
            versionAsString = stmFirmwareVersion.toNewVersionNamingScheme(),
        ),
        un20AppVersion = Un20ExtendedAppVersion(
            versionAsString = un20AppVersion.toNewVersionNamingScheme(),
        ),
    )

    companion object {
        fun fromBytes(bytes: ByteArray) = with(RootMessageProtocol) {
            UnifiedVersionInformation(
                masterFirmwareVersion = bytes.extract({ long }, 0..7),
                cypressFirmwareVersion = CypressFirmwareVersion.fromBytes(bytes.sliceArray(8..15)),
                stmFirmwareVersion = StmFirmwareVersion.fromBytes(bytes.sliceArray(16..23)),
                un20AppVersion = Un20AppVersion.fromBytes(bytes.sliceArray(24..31)),
            )
        }
    }
}
