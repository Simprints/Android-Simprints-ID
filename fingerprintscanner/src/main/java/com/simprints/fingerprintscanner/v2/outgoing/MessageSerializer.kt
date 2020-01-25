package com.simprints.fingerprintscanner.v2.outgoing

import com.simprints.fingerprintscanner.v2.domain.Message

interface MessageSerializer<in C : Message> {

    fun serialize(message: C): List<ByteArray>
}
