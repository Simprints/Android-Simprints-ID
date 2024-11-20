package com.simprints.fingerprint.infra.scannermock.simulated.v2.response

import com.simprints.fingerprint.infra.scanner.v2.domain.IncomingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.OutgoingMessage

interface SimulatedResponseHelperV2<T : OutgoingMessage, R : IncomingMessage> {

    fun createResponseToCommand(command: T): R
}
