package com.simprints.fingerprint.infra.scanner.v2.incoming.main

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.testtools.packetWithSourceAndPayload
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetSupportedTemplateTypesResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Packet
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route.Remote.Un20Server
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route.Remote.VeroEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.packet.Route.Remote.VeroServer
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainMessageInputStreamTest {
    private val packetRouter: PacketRouter = mockk {
        justRun { connect(any()) }
        justRun { disconnect() }
    }
    private val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
    private val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
    private val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
    private lateinit var messageInputStream: MainMessageInputStream

    @Before
    fun setup() {
        messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator,
        )
    }

    @Test
    fun messageInputStream_receiveVeroResponse_correctlyForwardsResponse() = runTest {
        val messageBytes = "20 10 01 00 FF".hexToByteArray()
        val packetsFlow = MutableSharedFlow<Packet>()

        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)
        val routes = mapOf(
            VeroServer as Route to packetsFlow,
            VeroEvent as Route to emptyFlow(),
            Un20Server as Route to emptyFlow(),
        )
        every { packetRouter.incomingPacketRoutes } returns routes
        messageInputStream.connect(mockk())
        launch { packetsFlow.emitAll(VeroServer, messageBytes) }
        val res = messageInputStream.receiveResponse<GetUn20OnResponse>()
        assertThat(res.value).isEqualTo(expectedResponse.value)
    }

    @Test
    fun messageInputStream_receiveUn20Response_correctlyForwardsResponse() = runTest {
        val messageBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val packetsFlow = MutableSharedFlow<Packet>()

        val expectedResponse =
            GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val routes = mapOf(
            VeroServer as Route to emptyFlow(),
            VeroEvent as Route to emptyFlow(),
            Un20Server as Route to packetsFlow,
        )
        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())
        launch { packetsFlow.emitAll(Un20Server, messageBytes) }
        val response = messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>()
        assertThat(response.supportedTemplateTypes).isEqualTo(
            expectedResponse.supportedTemplateTypes,
        )
    }

    @Test
    fun messageInputStream_subscribeToVeroEvents_correctlyForwardsEvents() = runTest {
        val numberOfEvents = 5
        val messageBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val packetsFlow = MutableSharedFlow<Packet>()
        val expectedEvent = TriggerButtonPressedEvent()

        val routes = mapOf(
            VeroServer as Route to emptyFlow(),
            VeroEvent as Route to packetsFlow,
            Un20Server as Route to emptyFlow(),
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())
        launch { packetsFlow.emitAll(VeroEvent, messageBytes, 3) }
        val response = messageInputStream.veroEvents
        assertThat(response.first()).isInstanceOf(expectedEvent::class.java)
    }

    @Test
    fun messageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() = runTest {
        val messageBytes = "20 10 01 00 FF 20 10 01 00 00".hexToByteArray()
        val packetsFlow = MutableSharedFlow<Packet>()
        val firstExpectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val routes = mapOf(
            VeroServer as Route to packetsFlow,
            VeroEvent as Route to emptyFlow(),
            Un20Server as Route to emptyFlow(),
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())
        launch { packetsFlow.emitAll(VeroServer, messageBytes) }
        val response = messageInputStream.receiveResponse<GetUn20OnResponse>()
        assertThat(response.value).isEqualTo(firstExpectedResponse.value)
    }

    @Test
    fun messageInputStream_receiveDifferentResponses_forwardsOnlyCorrectResponse() = runTest {
        val messageBytes = "20 20 01 00 00 30 10 01 00 FF 20 10 01 00 FF".hexToByteArray()
        val packetsFlow = MutableSharedFlow<Packet>()
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val routes = mapOf(
            VeroServer as Route to packetsFlow,
            VeroEvent as Route to emptyFlow(),
            Un20Server as Route to emptyFlow(),
        )
        every { packetRouter.incomingPacketRoutes } returns routes
        messageInputStream.connect(mockk())
        launch { packetsFlow.emitAll(VeroServer, messageBytes) }
        val response = messageInputStream.receiveResponse<GetUn20OnResponse>()
        assertThat(response.value).isEqualTo(expectedResponse.value)
    }

    @Test
    fun messageInputStream_receiveResponsesFromMultipleRoutes_correctlyForwardIt() = runTest {
        val veroResponseBytes = "20 10 01 00 FF".hexToByteArray()
        val expectedVeroResponse = GetUn20OnResponse(DigitalValue.TRUE)
        val numberOfEvents = 5
        val veroEventBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val expectedVeroEvent = TriggerButtonPressedEvent()
        val un20ResponseBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val expectedUn20Response =
            GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val veroResponsePackets = MutableSharedFlow<Packet>()
        val veroEventPackets = MutableSharedFlow<Packet>()
        val un20ResponsePackets = MutableSharedFlow<Packet>()

        val routes = mapOf<Route, Flow<Packet>>(
            VeroServer to veroResponsePackets,
            VeroEvent to veroEventPackets,
            Un20Server to un20ResponsePackets,
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())
        launch { veroResponsePackets.emitAll(VeroServer, veroResponseBytes) }
        val veroResponse = messageInputStream.receiveResponse<GetUn20OnResponse>()
        launch { veroEventPackets.emitAll(VeroEvent, veroEventBytes, 3) }
        val veroEvent = messageInputStream.receiveResponse<TriggerButtonPressedEvent>()
        launch { un20ResponsePackets.emitAll(Un20Server, un20ResponseBytes) }
        val un20Response = messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>()

        assertThat(veroResponse.value).isEqualTo(expectedVeroResponse.value)
        assertThat(veroEvent.veroMessageType).isEqualTo(expectedVeroEvent.veroMessageType)
        assertThat(un20Response.supportedTemplateTypes).isEqualTo(expectedUn20Response.supportedTemplateTypes)
    }

    @Test
    fun messageInputStream_disconnect_disconnectsPacketRouter() = runTest {
        val routes = mapOf(
            VeroServer as Route to emptyFlow<Packet>(),
            VeroEvent as Route to emptyFlow(),
            Un20Server as Route to emptyFlow(),
        )

        every { packetRouter.incomingPacketRoutes } returns routes
        justRun { packetRouter.disconnect() }
        messageInputStream.connect(mockk())
        messageInputStream.disconnect()
        verify {
            packetRouter.disconnect()
        }
    }

    private suspend fun MutableSharedFlow<Packet>.emitAll(
        route: Route,
        bytes: ByteArray,
        chunkSize: Int = 2,
    ) {
        bytes.chunked(chunkSize).forEach { emit(packetWithSourceAndPayload(route, it)) }
    }
}
