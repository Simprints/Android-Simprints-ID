package com.simprints.fingerprint.infra.scanner.testtools

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.PacketProtocol
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketParser
import kotlin.random.Random

fun hollowPacketWithRawBytes(bytes: ByteArray) =
    Packet(bytes, byteArrayOf(), byteArrayOf(), 0x00, 0x00, 0x00)

fun hollowPacketWithPayload(payload: ByteArray) =
    Packet(byteArrayOf(), byteArrayOf(), payload, 0x00, 0x00, 0x00)

fun packetWithSourceAndPayload(source: Route, payload: ByteArray) =
    PacketProtocol
        .buildPacketBytes(source, Route.Local.AndroidDevice, payload).let {
            PacketParser().parse(it)
        }

fun randomPacketWithSource(source: Route): Packet =
    PacketProtocol
        .buildPacketBytes(source, Route.Local.AndroidDevice, randomPayload()).let {
            PacketParser().parse(it)
        }

fun randomHollowPacketWithRawBytes(maxSize: Int = 20) =
    hollowPacketWithRawBytes(Random.nextBytes(Random.nextInt(1, maxSize)))

fun randomPacketsWithSource(source: Route, maxSize: Int = 20): List<Packet> =
    List(Random.nextInt(1, maxSize)) { randomPacketWithSource(source) }

fun randomPayload(maxSize: Int = 20) = Random.nextBytes(Random.nextInt(maxSize))

fun assertPacketsEqual(expected: List<Packet>, actual: List<Packet>) {
    assertThat(actual.size).isEqualTo(expected.size)
    expected.zip(actual).forEach { (expected, actual) ->
        assertPacketsEqual(expected, actual)
    }
}

fun assertPacketsEqual(expected: Packet, actual: Packet) {
    assertThat(actual.bytes).isEqualTo(expected.bytes)
    assertThat(actual.header).isEqualTo(expected.header)
    assertThat(actual.payload).isEqualTo(expected.payload)
    assertThat(actual.source).isEqualTo(expected.source)
    assertThat(actual.destination).isEqualTo(expected.destination)
    assertThat(actual.payloadLength).isEqualTo(expected.payloadLength)
}
