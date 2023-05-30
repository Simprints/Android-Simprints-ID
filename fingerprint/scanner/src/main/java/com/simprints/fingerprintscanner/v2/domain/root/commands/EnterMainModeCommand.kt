package com.simprints.fingerprintscanner.v2.domain.root.commands

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType

class EnterMainModeCommand : RootCommand(RootMessageType.ENTER_MAIN_MODE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = EnterMainModeCommand()
    }
}
