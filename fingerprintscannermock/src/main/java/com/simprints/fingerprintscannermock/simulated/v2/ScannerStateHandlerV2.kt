package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.Mode
import com.simprints.fingerprintscanner.v2.domain.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.Un20Command
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.VeroCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetSmileLedStateCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetTriggerButtonActiveCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.SetUn20OnCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue.FALSE
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue.TRUE
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterCypressOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterMainModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.EnterStmOtaModeCommand
import com.simprints.fingerprintscanner.v2.domain.root.commands.SetVersionCommand

fun SimulatedScannerStateV2.updateStateAccordingToOutgoingMessage(command: OutgoingMessage) {
    when (command) {
        is RootCommand -> {
            when (command) {
                is EnterMainModeCommand -> {
                    mode = Mode.MAIN
                }
                is EnterCypressOtaModeCommand -> {
                    mode = Mode.CYPRESS_OTA
                }
                is EnterStmOtaModeCommand -> {
                    mode = Mode.STM_OTA
                }
                is SetVersionCommand -> {
                    versionInfo = command.version
                }
                else -> {
                    /* do nothing */
                }
            }
        }
        is VeroCommand -> {
            when (command) {
                is SetUn20OnCommand -> {
                    isUn20On = when (command.value) {
                        FALSE -> false
                        TRUE -> true
                    }.also { eventQueue.add { (this as SimulatedScannerV2).triggerUn20StateChangeEvent(command.value) } }
                }
                is SetTriggerButtonActiveCommand -> {
                    isTriggerButtonActive = when (command.value) {
                        FALSE -> false
                        TRUE -> true
                    }
                }
                is SetSmileLedStateCommand -> {
                    smileLedState = command.smileLedState
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
