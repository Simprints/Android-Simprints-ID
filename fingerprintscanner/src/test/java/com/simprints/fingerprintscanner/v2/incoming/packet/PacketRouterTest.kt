package com.simprints.fingerprintscanner.v2.incoming.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.assertPacketsEqual
import com.simprints.fingerprintscanner.testtools.interleave
import com.simprints.fingerprintscanner.testtools.randomPacketsWithSource
import com.simprints.fingerprintscanner.v2.domain.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.tools.lang.objects
import com.simprints.testtools.unit.reactive.testSubscribe
import io.reactivex.subscribers.TestSubscriber
import org.junit.Before
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class PacketRouterTest {

    lateinit var outputStream: PipedOutputStream
    lateinit var inputStream: PipedInputStream
    lateinit var router: PacketRouter

    @Before
    fun setUp() {
        outputStream = PipedOutputStream()
        inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        router = PacketRouter(ByteArrayToPacketAccumulator(PacketParser()))

        router.connect(inputStream)
    }

    @Test
    fun packetRouter_connectAndDisconnect_correctlyConfiguresStreams() {
        assertThat(router.incomingPacketChannels.keys).containsExactlyElementsIn(Channel.Remote::class.objects())
    }

    @Test
    fun packetRouter_receivingPacketsInterleavedFromDifferentSources_routesCorrectly() {
        val veroResponseTestSubscriber = TestSubscriber<Packet>()
        val veroEventTestSubscriber = TestSubscriber<Packet>()
        val un20ResponseTestSubscriber = TestSubscriber<Packet>()

        router.incomingPacketChannels[Channel.Remote.VeroServer]?.testSubscribe(veroResponseTestSubscriber)
        router.incomingPacketChannels[Channel.Remote.VeroEvent]?.testSubscribe(veroEventTestSubscriber)
        router.incomingPacketChannels[Channel.Remote.Un20Server]?.testSubscribe(un20ResponseTestSubscriber)

        val veroResponsePackets = randomPacketsWithSource(Channel.Remote.VeroServer)
        val veroEventPackets = randomPacketsWithSource(Channel.Remote.VeroEvent)
        val un20ResponsePackets = randomPacketsWithSource(Channel.Remote.Un20Server)
        val allPackets = interleave(veroResponsePackets, veroEventPackets, un20ResponsePackets)

        allPackets.forEach { outputStream.write(it.bytes) }

        veroResponseTestSubscriber.awaitCount(veroResponsePackets.count())
        veroEventTestSubscriber.awaitCount(veroEventPackets.count())
        un20ResponseTestSubscriber.awaitCount(un20ResponsePackets.count())

        assertPacketsEqual(veroResponsePackets, veroResponseTestSubscriber.values())
        assertPacketsEqual(veroEventPackets, veroEventTestSubscriber.values())
        assertPacketsEqual(un20ResponsePackets, un20ResponseTestSubscriber.values())
    }

    @Test
    fun packetRouter_receivingPacketsThenDisconnected_noLongerForwardsPackets() {
        // TODO
    }

    @Test
    fun packetRouter_packetsReceivedBeforeSubscribed_doesNotRecoupPackets() {
        // TODO
    }
}
