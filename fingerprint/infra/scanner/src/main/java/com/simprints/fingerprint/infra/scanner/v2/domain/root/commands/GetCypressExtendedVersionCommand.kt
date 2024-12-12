package com.simprints.fingerprint.infra.scanner.v2.domain.root.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType

class GetCypressExtendedVersionCommand : RootCommand(RootMessageType.GET_CYPRESS_EXTENDED_VERSION) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetCypressExtendedVersionCommand()
    }
}
