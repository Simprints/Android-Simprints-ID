package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue.FALSE
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue.TRUE
import com.simprints.fingerprintscannermock.simulated.common.ScannerState

fun ScannerState.updateStateAccordingToOutgoingMessage(command: OutgoingMessage) {
    when (command) {
        is VeroCommand -> {
            when (command) {
                is SetUn20OnCommand -> {
                    isUn20On = when (command.value) {
                        FALSE -> false
                        TRUE -> true
                    }.also { eventQueue.add { (this as SimulatedScannerV2).triggerUn20StateChangeEvent(command.value) } }
                }
                else -> {
                    /* do nothing */
                }
            }
        }
        is Un20Command -> {
            /* do nothing */
        }
        else -> {
            /* do nothing */
        }
    }
}
