package com.simprints.fingerprintscanner.v2.outgoing.main.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.randomHollowPacketWithRawBytes
import org.junit.Test

class PacketSerializerTest {

    @Test
    fun packetSerializer_serializesCorrectly() {
        val packet = randomHollowPacketWithRawBytes()
        val expected = packet.bytes

        val packetSerializer = PacketSerializer()

        assertThat(packetSerializer.serialize(packet)).isEqualTo(expected)
    }
}
