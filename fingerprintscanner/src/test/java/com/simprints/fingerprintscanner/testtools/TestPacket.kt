package com.simprints.fingerprintscanner.testtools

import com.simprints.fingerprintscanner.v2.domain.packet.Packet

fun hollowPacketWithRawBytes(bytes: ByteArray) =
    Packet(bytes, byteArrayOf(), byteArrayOf(), 0x00, 0x00, 0x00)

fun hollowPacketWithPayload(payload: ByteArray) =
    Packet(byteArrayOf(), byteArrayOf(), payload, 0x00, 0x00, 0x00)
