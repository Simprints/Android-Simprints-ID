package com.simprints.fingerprintscanner.v2.incoming.main.packet

import com.simprints.fingerprintscanner.testtools.assertPacketsEqual
import com.simprints.fingerprintscanner.testtools.interleave
import com.simprints.fingerprintscanner.testtools.randomPacketsWithSource
import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
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

        router = PacketRouter(Route.Remote::class.objects(), { source }, ByteArrayToPacketAccumulator(PacketParser()))

        router.connect(inputStream)
    }

    @Test
    fun packetRouter_receivingPacketsInterleavedFromDifferentSources_routesCorrectly() {
        val veroResponseTestSubscriber = router.incomingPacketRoutes[Route.Remote.VeroServer]?.testSubscribe()
            ?: failTest("Missing route")
        val veroEventTestSubscriber = router.incomingPacketRoutes[Route.Remote.VeroEvent]?.testSubscribe()
            ?: failTest("Missing route")
        val un20ResponseTestSubscriber = router.incomingPacketRoutes[Route.Remote.Un20Server]?.testSubscribe()
            ?: failTest("Missing route")

        val veroResponsePackets = randomPacketsWithSource(Route.Remote.VeroServer)
        val veroEventPackets = randomPacketsWithSource(Route.Remote.VeroEvent)
        val un20ResponsePackets = randomPacketsWithSource(Route.Remote.Un20Server)
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
