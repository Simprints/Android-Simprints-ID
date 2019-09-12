package com.simprints.fingerprintscanner.v2.message

interface MessageParser {

    fun parse(bytes: ByteArray): Message
}
