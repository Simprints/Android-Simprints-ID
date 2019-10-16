package com.simprints.fingerprintscanner.v2.incoming.packet

import com.simprints.fingerprintscanner.testtools.assertPacketsEqual
import com.simprints.fingerprintscanner.testtools.interleave
import com.simprints.fingerprintscanner.testtools.randomPacketsWithSource
import com.simprints.fingerprintscanner.v2.domain.packet.Channel
import com.simprints.fingerprintscanner.v2.tools.lang.objects
import com.simprints.testtools.common.syntax.failTest
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Before
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream

class PacketRouterTest {

    private lateinit var outputStream: PipedOutputStream
    private lateinit var inputStream: PipedInputStream
    private lateinit var router: PacketRouter

    @Before
    fun setUp() {
        outputStream = PipedOutputStream()
        inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        router = PacketRouter(Channel.Remote::class.objects(), ByteArrayToPacketAccumulator(PacketParser()))

        router.connect(inputStream)
    }

    @Test
    fun packetRouter_receivingPacketsInterleavedFromDifferentSources_routesCorrectly() {
        val veroResponseTestSubscriber = router.incomingPacketChannels[Channel.Remote.VeroServer]?.testSubscribe()
            ?: failTest("Missing channel")
        val veroEventTestSubscriber = router.incomingPacketChannels[Channel.Remote.VeroEvent]?.testSubscribe()
            ?: failTest("Missing channel")
        val un20ResponseTestSubscriber = router.incomingPacketChannels[Channel.Remote.Un20Server]?.testSubscribe()
            ?: failTest("Missing channel")

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
}
