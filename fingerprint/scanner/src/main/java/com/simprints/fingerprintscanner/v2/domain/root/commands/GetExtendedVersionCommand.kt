package com.simprints.fingerprintscanner.v2.domain.root.commands

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType

class GetExtendedVersionCommand: RootCommand(RootMessageType.GET_EXTENDED_VERSION) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = GetExtendedVersionCommand()
    }
}

