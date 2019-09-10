package com.simprints.fingerprintscanner.v2.packets

class Packet(
    val bytes: ByteArray,
    val header: ByteArray,
    val body: ByteArray,
    val source: Int,
    val destination: Int,
    val length: Int
)
