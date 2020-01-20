package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMessage

interface SimulatedResponseHelperV2<T : OutgoingMessage, R : IncomingMessage> {

    fun createResponseToCommand(command: T): R
}
