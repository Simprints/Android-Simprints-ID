package com.simprints.fingerprint.infra.scanner.v2.domain.root.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation

class SetExtendedVersionCommand(
    val version: ExtendedVersionInformation,
) : RootCommand(RootMessageType.SET_EXTENDED_VERSION) {
    override fun getDataBytes(): ByteArray = version.getBytes()

    companion object {
        fun fromBytes(data: ByteArray) = SetExtendedVersionCommand(
            ExtendedVersionInformation.fromBytes(data),
        )
    }
}
