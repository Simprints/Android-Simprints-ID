package com.simprints.fingerprintscanner.v2.domain.root.commands

import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootMessageType

class EnterStmOtaModeCommand : RootCommand(RootMessageType.ENTER_STM_OTA_MODE) {

    companion object {
        fun fromBytes(@Suppress("unused_parameter") data: ByteArray) = EnterStmOtaModeCommand()
    }
}
