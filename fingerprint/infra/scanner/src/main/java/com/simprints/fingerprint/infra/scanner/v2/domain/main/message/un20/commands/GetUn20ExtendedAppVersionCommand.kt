package com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands

import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20MessageType

class GetUn20ExtendedAppVersionCommand : Un20Command(Un20MessageType.GetUn20ExtendedAppVersion) {
    companion object {
        fun fromBytes(
            @Suppress("unused_parameter") data: ByteArray,
        ) = GetUn20ExtendedAppVersionCommand()
    }
}
