package com.simprints.fingerprintscannermock.simulated.v2.response

import com.simprints.fingerprintscanner.v2.domain.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.OutgoingMessage

interface SimulatedResponseHelperV2<T : OutgoingMessage, R : IncomingMessage> {

    fun createResponseToCommand(command: T): R
}
