package com.simprints.fingerprintscanner.v2.message

class MessageParser {

    fun parse(bytes: ByteArray) = Message(bytes)
}
