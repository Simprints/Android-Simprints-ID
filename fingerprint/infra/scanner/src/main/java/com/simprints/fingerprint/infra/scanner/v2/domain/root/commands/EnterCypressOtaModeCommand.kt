package com.simprints.fingerprint.infra.scanner.v2.domain.root.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootMessageType

class EnterCypressOtaModeCommand : RootCommand(RootMessageType.ENTER_CYPRESS_OTA_MODE) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = EnterCypressOtaModeCommand()
    }
}
