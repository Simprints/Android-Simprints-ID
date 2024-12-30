package com.simprints.fingerprint.infra.scanner.v2.outgoing.common

import com.simprints.fingerprint.infra.scanner.v2.domain.Message
import com.simprints.fingerprint.infra.scanner.v2.outgoing.OutgoingConnectable

/**
 * Base high-level class for sending messages to the scanner via a [java.io.OutputStream]
 */
abstract class MessageOutputStream<in T : Message>(
    private val messageSerializer: MessageSerializer<T>,
    private val outputStreamDispatcher: OutputStreamDispatcher,
) : OutgoingConnectable by outputStreamDispatcher {
    fun sendMessage(message: T) {
        val serializedMessage = messageSerializer.serialize(message)
        outputStreamDispatcher.dispatch(serializedMessage)
    }
}
