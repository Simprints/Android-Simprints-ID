package com.simprints.fingerprintscanner.v2.packets

class Packet(
    val bytes: ByteArray,
    val header: ByteArray,
    val payload: ByteArray,
    val source: Int,
    val destination: Int,
    val payloadLength: Int
)
