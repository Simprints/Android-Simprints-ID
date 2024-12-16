package com.simprints.fingerprint.infra.scanner.v2.outgoing.common

import com.simprints.fingerprint.infra.scanner.v2.domain.Message

/**
 * Interface for converting messages of type [T] into a List<ByteArray> which represents the list of
 * packets to be sent over the Bluetooth output stream
 */
interface MessageSerializer<in T : Message> {
    fun serialize(message: T): List<ByteArray>
}
