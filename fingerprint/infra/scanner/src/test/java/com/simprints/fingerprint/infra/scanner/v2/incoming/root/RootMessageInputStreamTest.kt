package com.simprints.fingerprint.infra.scanner.v2.incoming.root

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.root.RootResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.EnterMainModeResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.helpers.SchedulerHelper.TIMEOUT
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.chunked
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.BaseTestConsumer.TestWaitStrategy
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

class RootMessageInputStreamTest {

    private val rootMessageAccumulator = RootResponseAccumulator(RootResponseParser())
    private val rootMessageInputStream = RootMessageInputStream(rootMessageAccumulator)

    @Test
    fun `test disconnect disposes the flowable stream`(){
        //Given
        mockkStatic("com.simprints.fingerprint.infra.scanner.v2.incoming.root.RootMessageStreamKt")
        val flowableDisposable = mockk<Disposable>(relaxed = true)

        val rootResponseFlowable: Flowable<RootResponse> = mockk {
            every { subscribeOn(any()) } returns this
            every { publish() } returns mockk {
                every { connect() } returns flowableDisposable
            }
        }

        val flowable:Flowable<ByteArray> = mockk{
            every { toRootMessageStream(rootMessageAccumulator) } returns rootResponseFlowable
        }

        //When
        rootMessageInputStream.connect(flowable)
        rootMessageInputStream.disconnect()

        //Then
        verify { flowableDisposable.dispose() }
    }

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun rootMessageInputStream_receiveRootResponse_correctlyForwardsResponse() {
        val messageBytes = "F0 10 00 00".hexToByteArray()
        val packets = messageBytes.chunked(2)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream.toFlowable())

        val testSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
            .timeout(TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        packets.forEach { outputStream.write(it) }

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun rootMessageInputStream_receiveMultipleOfSameResponses_forwardsOnlyFirstAsResponse() {
        val messageBytes = "F0 10 00 00 F0 10 00 00 F0 10 00 00".hexToByteArray()
        val packets = messageBytes.chunked(3)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream.toFlowable())

        val testResponseSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
            .timeout(TIMEOUT, TimeUnit.SECONDS).testSubscribe()
        val testStreamSubscriber = rootMessageInputStream.rootResponseStream!!
            .timeout(TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        packets.forEach { outputStream.write(it) }

        testResponseSubscriber.awaitAndAssertSuccess()
        testStreamSubscriber.awaitCount(3, TestWaitStrategy.SLEEP_10MS, TIMEOUT)
        assertThat(testResponseSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
        assertThat(testStreamSubscriber.valueCount()).isEqualTo(3)
    }

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun rootMessageInputStream_receiveDifferentResponses_forwardsOnlyCorrectResponse() {
        val messageBytes = "F0 20 00 00 F0 10 00 00 F0 30 00 00".hexToByteArray()
        val packets = messageBytes.chunked(3)
        val expectedResponse = EnterMainModeResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        rootMessageInputStream.connect(inputStream.toFlowable())

        val testResponseSubscriber = rootMessageInputStream.receiveResponse<EnterMainModeResponse>()
            .timeout(TIMEOUT, TimeUnit.SECONDS).testSubscribe()
        val testStreamSubscriber = rootMessageInputStream.rootResponseStream!!
            .timeout(TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        packets.forEach { outputStream.write(it) }

        testResponseSubscriber.awaitAndAssertSuccess()
        testStreamSubscriber.awaitCount(3, TestWaitStrategy.SLEEP_10MS, TIMEOUT)
        assertThat(testResponseSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
        assertThat(testStreamSubscriber.valueCount()).isEqualTo(3)
    }
}
