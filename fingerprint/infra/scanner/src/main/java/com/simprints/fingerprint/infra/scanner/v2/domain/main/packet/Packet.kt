package com.simprints.fingerprint.infra.scanner.v2.domain.main.packet

class Packet(
    val bytes: ByteArray,
    val header: ByteArray,
    val payload: ByteArray,
    val source: Byte,
    val destination: Byte,
    val payloadLength: Int,
)
