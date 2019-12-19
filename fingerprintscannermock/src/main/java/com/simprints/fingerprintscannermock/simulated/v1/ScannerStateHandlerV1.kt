package com.simprints.fingerprintscannermock.simulated.v1

import com.simprints.fingerprintscanner.v1.Message
import com.simprints.fingerprintscanner.v1.enums.MESSAGE_TYPE
import com.simprints.fingerprintscannermock.simulated.common.ScannerState

fun ScannerState.updateStateAccordingToOutgoingMessage(message: Message) {
    when (message.messageType) {
        MESSAGE_TYPE.UN20_WAKEUP -> isUn20On = true
        MESSAGE_TYPE.UN20_SHUTDOWN -> isUn20On = false
        else -> {
            /* do nothing */
        }
    }
}
