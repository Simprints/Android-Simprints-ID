package com.simprints.fingerprintscanner.v2.outgoing.common

import com.simprints.fingerprintscanner.v2.domain.Message

interface MessageSerializer<in T : Message> {

    fun serialize(message: T): List<ByteArray>
}
