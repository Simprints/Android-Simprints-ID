package com.simprints.fingerprintscanner.v2.domain.main.message

interface Message {

    fun getBytes(): ByteArray
}

interface OutgoingMessage: Message

interface IncomingMessage: Message
