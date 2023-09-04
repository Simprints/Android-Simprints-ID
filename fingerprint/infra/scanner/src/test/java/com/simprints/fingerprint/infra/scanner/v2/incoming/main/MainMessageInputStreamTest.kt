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
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprint.infra.scanner.v2.tools.helpers.SchedulerHelper.INTERVAL
import com.simprints.fingerprint.infra.scanner.v2.tools.helpers.SchedulerHelper.TIMEOUT
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.reactivex.Flowable
import io.reactivex.rxkotlin.toFlowable
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.util.concurrent.TimeUnit

class MainMessageInputStreamTest {
    private val packetRouter = mockk<PacketRouter> {
        justRun { connect(any()) }
    }

    @Suppress(
        "Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
            " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
            "re-written when the RxJava is finally removed from the scanners SDK."
    )
    fun messageInputStream_receiveVeroResponse_correctlyForwardsResponse() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val messageBytes = "20 10 01 00 FF".hexToByteArray()
        val packets =
            messageBytes.chunked(2).map { packetWithSourceAndPayload(Route.Remote.VeroServer, it) }
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val routes = mapOf(
            Route.Remote.VeroServer as Route to packets.toFlowable().observeOn(testScheduler)
                .publish(),
            Route.Remote.VeroEvent as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.Un20Server as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val testSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>()
            .observeOn(testScheduler).timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes[Route.Remote.VeroServer]?.connect()

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!testSubscriber.isTerminated)

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.value == it.value }
    }

    @Suppress(
        "Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
            " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
            "re-written when the RxJava is finally removed from the scanners SDK."
    )
    fun messageInputStream_receiveUn20Response_correctlyForwardsResponse() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val messageBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val packets =
            messageBytes.chunked(2).map { packetWithSourceAndPayload(Route.Remote.Un20Server, it) }
        val expectedResponse =
            GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val routes = mapOf(
            Route.Remote.VeroServer as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.VeroEvent as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.Un20Server as Route to packets.toFlowable().observeOn(testScheduler)
                .publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val testSubscriber = messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>()
            .observeOn(testScheduler).timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes[Route.Remote.Un20Server]?.connect()

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!testSubscriber.isTerminated)

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.supportedTemplateTypes == it.supportedTemplateTypes }
    }

    @Test
    fun messageInputStream_subscribeToVeroEvents_correctlyForwardsEvents() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val numberOfEvents = 5
        val messageBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val packets =
            messageBytes.chunked(3).map { packetWithSourceAndPayload(Route.Remote.VeroEvent, it) }
        val expectedEvent = TriggerButtonPressedEvent()

        val routes = mapOf(
            Route.Remote.VeroServer as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.VeroEvent as Route to packets.toFlowable().observeOn(testScheduler)
                .publish(),
            Route.Remote.Un20Server as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val testSubscriber = messageInputStream.veroEvents!!.observeOn(testScheduler)
            .timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes[Route.Remote.VeroEvent]?.connect()

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!testSubscriber.isTerminated)

        testSubscriber.awaitCount(numberOfEvents)
        testSubscriber.values().forEach { assertThat(it).isInstanceOf(expectedEvent::class.java) }
    }

    @Test
    fun messageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val messageBytes = "20 10 01 00 FF 20 10 01 00 00".hexToByteArray()
        val packets =
            messageBytes.chunked(2).map { packetWithSourceAndPayload(Route.Remote.VeroServer, it) }
        val firstExpectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val routes = mapOf(
            Route.Remote.VeroServer as Route to packets.toFlowable().observeOn(testScheduler)
                .publish(),
            Route.Remote.VeroEvent as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.Un20Server as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val responseSubscriber = messageInputStream.receiveResponse<GetUn20OnResponse>()
            .observeOn(testScheduler).timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes[Route.Remote.VeroServer]?.connect()

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!responseSubscriber.isTerminated)

        responseSubscriber.awaitAndAssertSuccess()
        responseSubscriber.assertValue { firstExpectedResponse.value == it.value }
    }

    @Test
    fun messageInputStream_receiveDifferentResponses_forwardsOnlyCorrectResponse() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val messageBytes = "20 20 01 00 00 30 10 01 00 FF 20 10 01 00 FF".hexToByteArray()
        val packets =
            messageBytes.chunked(2).map { packetWithSourceAndPayload(Route.Remote.VeroServer, it) }
        val expectedResponse = GetUn20OnResponse(DigitalValue.TRUE)

        val routes = mapOf(
            Route.Remote.VeroServer as Route to packets.toFlowable().observeOn(testScheduler)
                .publish(),
            Route.Remote.VeroEvent as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish(),
            Route.Remote.Un20Server as Route to Flowable.empty<Packet>().observeOn(testScheduler)
                .publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val testSubscriber =
            messageInputStream.receiveResponse<GetUn20OnResponse>().observeOn(testScheduler)
                .timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes[Route.Remote.VeroServer]?.connect()

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!testSubscriber.isTerminated)

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue { expectedResponse.value == it.value }
    }

    @Suppress(
        "Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
            " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
            "re-written when the RxJava is finally removed from the scanners SDK."
    )
    fun messageInputStream_receiveResponsesAndEventsFromMultipleRoutesSimultaneously_correctlyForwardsResponsesAndEvents() {
        val testScheduler = TestScheduler()
        val veroResponseAccumulator = VeroResponseAccumulator(VeroResponseParser())
        val veroEventAccumulator = VeroEventAccumulator(VeroEventParser())
        val un20ResponseAccumulator = Un20ResponseAccumulator(Un20ResponseParser())
        val messageInputStream = MainMessageInputStream(
            packetRouter,
            veroResponseAccumulator,
            veroEventAccumulator,
            un20ResponseAccumulator
        )

        val veroResponseBytes = "20 10 01 00 FF".hexToByteArray()
        val expectedVeroResponse = GetUn20OnResponse(DigitalValue.TRUE)
        val numberOfEvents = 5
        val veroEventBytes = "3A 00 00 00".repeat(numberOfEvents).hexToByteArray()
        val expectedVeroEvent = TriggerButtonPressedEvent()
        val un20ResponseBytes = "30 00 01 00 00 00 10".hexToByteArray()
        val expectedUn20Response =
            GetSupportedTemplateTypesResponse(setOf(TemplateType.ISO_19794_2_2011))

        val veroResponsePackets = veroResponseBytes.chunked(2)
            .map { packetWithSourceAndPayload(Route.Remote.VeroServer, it) }
        val veroEventPackets =
            veroEventBytes.chunked(3).map { packetWithSourceAndPayload(Route.Remote.VeroEvent, it) }
        val un20ResponsePackets = un20ResponseBytes.chunked(2)
            .map { packetWithSourceAndPayload(Route.Remote.Un20Server, it) }

        val routes = mapOf(
            Route.Remote.VeroServer as Route to veroResponsePackets.toFlowable()
                .observeOn(testScheduler).publish(),
            Route.Remote.VeroEvent as Route to veroEventPackets.toFlowable()
                .observeOn(testScheduler).publish(),
            Route.Remote.Un20Server as Route to un20ResponsePackets.toFlowable()
                .observeOn(testScheduler).publish()
        )

        every { packetRouter.incomingPacketRoutes } returns routes

        messageInputStream.connect(mockk())

        val veroResponseTestSubscriber =
            messageInputStream.receiveResponse<GetUn20OnResponse>().observeOn(testScheduler)
                .timeout(TIMEOUT, TimeUnit.SECONDS).test()
        val veroEventTestSubscriber = messageInputStream.veroEvents!!.observeOn(testScheduler)
            .timeout(TIMEOUT, TimeUnit.SECONDS).test()
        val un20ResponseTestSubscriber =
            messageInputStream.receiveResponse<GetSupportedTemplateTypesResponse>()
                .observeOn(testScheduler).timeout(TIMEOUT, TimeUnit.SECONDS).test()

        routes.values.forEach { it.connect() }

        do {
            testScheduler.advanceTimeBy(INTERVAL, TimeUnit.MILLISECONDS)
        } while (!veroResponseTestSubscriber.isTerminated && !veroEventTestSubscriber.isTerminated && un20ResponseTestSubscriber.isTerminated)

        veroResponseTestSubscriber.awaitAndAssertSuccess()
        veroResponseTestSubscriber.assertValue { expectedVeroResponse.value == it.value }

        veroEventTestSubscriber.awaitCount(numberOfEvents)
        assertThat(veroEventTestSubscriber.values().size).isEqualTo(numberOfEvents)
        veroEventTestSubscriber.values()
            .forEach { assertThat(it).isInstanceOf(expectedVeroEvent::class.java) }

        un20ResponseTestSubscriber.awaitAndAssertSuccess()
        un20ResponseTestSubscriber.assertValue { expectedUn20Response.supportedTemplateTypes == it.supportedTemplateTypes }
    }
}
