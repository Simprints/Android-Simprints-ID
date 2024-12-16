package com.simprints.fingerprint.infra.scanner.v2.domain.root.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType

class GetHardwareVersionCommand : RootCommand(RootMessageType.GET_HARDWARE_VERSION) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetHardwareVersionCommand()
    }
}
