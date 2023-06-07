package com.simprints.fingerprintscanner.v2.domain

interface Message {

    fun getBytes(): ByteArray
}

interface OutgoingMessage: Message

interface IncomingMessage: Message
