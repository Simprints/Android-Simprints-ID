package com.simprints.fingerprintscanner.v2.incoming

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.testtools.chunked
import com.simprints.fingerprintscanner.testtools.packetWithSourceAndPayload
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.TemplateType
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.GetSupportedTemplateTypesResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.GetUn20OnResponse
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Packet
import com.simprints.fingerprintscanner.v2.incoming.main.MessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.whenever
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import org.junit.Test

class MessageInputStreamTest {

    private val packetRouter = mock<PacketRouter>()
    private val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
    private val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
    private val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
    private val messageInputStream = MessageInputStream(packetRouter, veroResponseAccumulator, veroEventAccumulator, un20ResponseAccumulator)

    @Test
    fun messageInputStream_receiveVeroResponse_correctlyForwardsResponse() {
        val messageBytes = "20 10 01 00 FF".hexToByteArray()
        val packets = messageBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.VeroServer, it) }
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to packets.toFlowable().publish(),
            Channel.Remote.VeroEvent as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.Un20Server as Channel to Flowable.empty<Packet>().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val testSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>().test()

        channels[Channel.Remote.VeroServer]?.connect()

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.value == it.value }
    }

    @Test
    fun messageInputStream_receiveUn20Response_correctlyForwardsResponse() {
        val messageBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val packets = messageBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.Un20Server, it) }
        val expectedResponse = GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.VeroEvent as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.Un20Server as Channel to packets.toFlowable().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val testSubscriber = messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>().test()

        channels[Channel.Remote.Un20Server]?.connect()

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.supportedTemplateTypes == it.supportedTemplateTypes }
    }

    @Test
    fun messageInputStream_subscribeToVeroEvents_correctlyForwardsEvents() {
        val numberOfEvents = 5
        val messageBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val packets = messageBytes.chunked(3).map { packetWithSourceAndPayload(Channel.Remote.VeroEvent, it) }
        val expectedEvent = TriggerButtonPressedEvent()

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.VeroEvent as Channel to packets.toFlowable().publish(),
            Channel.Remote.Un20Server as Channel to Flowable.empty<Packet>().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val testSubscriber = messageInputStream.veroEvents.test()

        channels[Channel.Remote.VeroEvent]?.connect()

        testSubscriber.awaitCount(numberOfEvents)
        testSubscriber.assertValueCount(numberOfEvents)
        testSubscriber.values().forEach { assertThat(it).isInstanceOf(expectedEvent::class.java) }
    }

    @Test
    fun messageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() {
        val messageBytes = "20 10 01 00 FF 20 10 01 00 00".hexToByteArray()
        val packets = messageBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.VeroServer, it) }
        val firstExpectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to packets.toFlowable().publish(),
            Channel.Remote.VeroEvent as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.Un20Server as Channel to Flowable.empty<Packet>().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val responseSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>().test()

        channels[Channel.Remote.VeroServer]?.connect()

        responseSubscriber.awaitAndAssertSuccess()
        responseSubscriber.assertValue { firstExpectedResponse.value == it.value }
    }

    @Test
    fun messageInputStream_receiveDifferentResponsesThenCorrect_correctlyForwardsResponse() {
        val messageBytes = "20 11 01 00 00 30 10 01 00 FF 20 10 01 00 FF".hexToByteArray()
        val packets = messageBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.VeroServer, it) }
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to packets.toFlowable().publish(),
            Channel.Remote.VeroEvent as Channel to Flowable.empty<Packet>().publish(),
            Channel.Remote.Un20Server as Channel to Flowable.empty<Packet>().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val testSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>().test()

        channels[Channel.Remote.VeroServer]?.connect()

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.value == it.value }
    }

    @Test
    fun messageInputStream_receiveResponsesAndEventsFromMultipleChannelsSimultaneously_correctlyForwardsResponsesAndEvents() {
        val veroResponseBytes = "20 10 01 00 FF".hexToByteArray()
        val expectedVeroResponse = GetUn20OnResponse(DigitalValue.TRUE)
        val numberOfEvents = 5
        val veroEventBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val expectedVeroEvent = TriggerButtonPressedEvent()
        val un20ResponseBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val expectedUn20Response = GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val veroResponsePackets = veroResponseBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.VeroServer, it) }
        val veroEventPackets = veroEventBytes.chunked(3).map { packetWithSourceAndPayload(Channel.Remote.VeroEvent, it) }
        val un20ResponsePackets = un20ResponseBytes.chunked(2).map { packetWithSourceAndPayload(Channel.Remote.Un20Server, it) }

        val channels = mapOf(
            Channel.Remote.VeroServer as Channel to veroResponsePackets.toFlowable().publish(),
            Channel.Remote.VeroEvent as Channel to veroEventPackets.toFlowable().publish(),
            Channel.Remote.Un20Server as Channel to un20ResponsePackets.toFlowable().publish())

        whenever(packetRouter) { incomingPacketChannels } thenReturn channels

        messageInputStream.connect(mock())

        val veroResponseTestSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>().test()
        val veroEventTestSubscriber = messageInputStream.veroEvents.test()
        val un20ResponseTestSubscriber = messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>().test()

        channels.values.forEach { it.connect() }

        veroResponseTestSubscriber.awaitAndAssertSuccess()
        veroResponseTestSubscriber.assertValue { expectedVeroResponse.value == it.value }

        veroEventTestSubscriber.awaitCount(numberOfEvents)
        veroEventTestSubscriber.assertValueCount(numberOfEvents)
        veroEventTestSubscriber.values().forEach { assertThat(it).isInstanceOf(expectedVeroEvent::class.java) }

        un20ResponseTestSubscriber.awaitAndAssertSuccess()
        un20ResponseTestSubscriber.assertValue { expectedUn20Response.supportedTemplateTypes == it.supportedTemplateTypes }
    }
}
