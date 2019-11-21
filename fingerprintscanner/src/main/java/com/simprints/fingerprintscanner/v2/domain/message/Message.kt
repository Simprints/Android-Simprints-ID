package com.simprints.fingerprintscanner.v2.domain.message

interface Message {

    fun getBytes(): ByteArray
}

interface OutgoingMessage: Message

interface IncomingMessage: Message
