package com.simprints.fingerprintscanner.v2.domain.packet

class Packet(
    val bytes: ByteArray,
    val header: ByteArray,
    val payload: ByteArray,
    val source: Byte,
    val destination: Byte,
    val payloadLength: Int
)
