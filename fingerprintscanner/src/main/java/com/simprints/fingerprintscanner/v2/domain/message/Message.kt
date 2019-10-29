package com.simprints.fingerprintscanner.v2.domain.message

interface Message

interface OutgoingMessage: Message {

    fun getBytes(): ByteArray
}

interface IncomingMessage: Message
