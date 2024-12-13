package com.simprints.fingerprint.infra.scanner.v2.domain

interface Message {
    fun getBytes(): ByteArray
}

interface OutgoingMessage : Message

interface IncomingMessage : Message
