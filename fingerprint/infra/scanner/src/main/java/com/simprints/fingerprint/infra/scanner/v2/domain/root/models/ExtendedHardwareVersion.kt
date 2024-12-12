package com.simprints.fingerprint.infra.scanner.v2.domain.root.models

class ExtendedHardwareVersion(
    val versionIdentifier: String,
) {
    companion object {
        fun fromBytes(bytes: ByteArray) = ExtendedHardwareVersion(
            versionIdentifier = String(bytes),
        )
    }
}
