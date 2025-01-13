package com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.testtools.randomPacketWithSource
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.tools.lang.objects
import com.simprints.testtools.common.syntax.failTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class PacketRouterTest {
    private lateinit var router: PacketRouter

    @Before
    fun setUp() {
        router = PacketRouter(
            Route.Remote::class.objects(),
            { source },
            ByteArrayToPacketAccumulator(PacketParser()),
            Dispatchers.IO,
        )
    }

    @Test
    fun `packetRouter receiving Packets from different sources routesCorrectly`() = runTest {
        val veroResponsePacket = randomPacketWithSource(Route.Remote.VeroServer).bytes
        val veroEventPacket = randomPacketWithSource(Route.Remote.VeroEvent).bytes
        val un20ResponsePacket = randomPacketWithSource(Route.Remote.Un20Server).bytes
        val allPacketsByteArrayFlow = MutableSharedFlow<ByteArray>()

        // Collect data in a different coroutine scope to avoid blocking
        router.connect(allPacketsByteArrayFlow)
        val veroResponseTestSubscriber = router.incomingPacketRoutes[Route.Remote.VeroServer] ?: failTest("Missing route")
        val un20ResponseTestSubscriber = router.incomingPacketRoutes[Route.Remote.Un20Server] ?: failTest("Missing route")
        launch {
            allPacketsByteArrayFlow.emit(veroResponsePacket)
            allPacketsByteArrayFlow.emit(veroEventPacket)
            allPacketsByteArrayFlow.emit(un20ResponsePacket)
        }
        assertThat(veroResponsePacket).isEqualTo(veroResponseTestSubscriber.first().bytes)
        assertThat(un20ResponsePacket).isEqualTo(un20ResponseTestSubscriber.first().bytes)
    }

    @Test
    fun `disconnect cancels job`() = runTest {
        router.connect(flowOf(byteArrayOf(0x01, 0x02)))

        router.disconnect()

        // Assert job  is cancelled
        assertThat(router.packetProcessingJob?.isCancelled).isTrue()
    }

    @Test
    fun `calling connect twice cancels the old job`() = runTest {
        val mockInputStream = flowOf(byteArrayOf(0x01, 0x02))

        // Call connect the first time
        router.connect(mockInputStream)
        val firstJob = router.packetProcessingJob

        // Call connect a second time
        router.connect(mockInputStream)
        val secondJob = router.packetProcessingJob

        // Ensure the first job was cancelled and a new job started
        assertThat(firstJob?.isCancelled).isTrue()
        assertThat(secondJob?.isCancelled).isFalse()
        assertThat(firstJob).isNotEqualTo(secondJob)
    }
}
