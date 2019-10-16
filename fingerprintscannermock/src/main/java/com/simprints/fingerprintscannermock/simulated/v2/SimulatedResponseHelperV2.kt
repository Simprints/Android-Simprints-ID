package com.simprints.fingerprintscannermock.simulated.v2

import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage

interface SimulatedResponseHelperV2<T : OutgoingMessage> {

    fun createResponseToCommand(command: T): ByteArray
}
