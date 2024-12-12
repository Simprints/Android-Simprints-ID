package com.simprints.fingerprint.infra.scanner.v2.domain.root.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType

class GetCypressVersionCommand : RootCommand(RootMessageType.GET_CYPRESS_VERSION) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetCypressVersionCommand()
    }
}
