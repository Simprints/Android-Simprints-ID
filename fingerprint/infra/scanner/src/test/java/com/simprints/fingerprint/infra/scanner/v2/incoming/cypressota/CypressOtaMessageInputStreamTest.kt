package com.simprints.fingerprint.infra.scanner.v2.incoming.cypressota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprint.infra.scanner.v2.tools.helpers.SchedulerHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.primitives.hexToByteArray
import com.simprints.fingerprint.infra.scanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

class CypressOtaMessageInputStreamTest {
    private val cypressOtaResponseParser = CypressOtaResponseParser()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val cypressOtaMessageInputStream = CypressOtaMessageInputStream(
        cypressOtaResponseParser,
        UnconfinedTestDispatcher(),
    )

    @Test
    fun `test disconnect disposes the flowable stream`() {
        // Given
        val flowableDisposable = mockk<Disposable>(relaxed = true)

        val cypressResponseFlowable: Flowable<CypressOtaResponse> = mockk {
            every { subscribeOn(any()) } returns this
            every { publish() } returns mockk {
                every { connect() } returns flowableDisposable
            }
        }

        val flowable: Flowable<ByteArray> = mockk {
            every { map(any<Function<ByteArray, CypressOtaResponse>>()) } returns cypressResponseFlowable
        }

        // When
        cypressOtaMessageInputStream.connect(flowable)
        cypressOtaMessageInputStream.disconnect()

        // Then
        verify { flowableDisposable.dispose() }
    }

    @Suppress(
        "Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
            " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
            "re-written when the RxJava is finally removed from the scanners SDK.",
    )
    fun cypressOtaMessageInputStream_receiveCypressOtaResponse_correctlyForwardsResponse() {
        val messageBytes = "39".hexToByteArray()
        val expectedResponse = ContinueResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        cypressOtaMessageInputStream.connect(inputStream.toFlowable())

        val testSubscriber = cypressOtaMessageInputStream
            .receiveResponse<CypressOtaResponse>()
            .timeout(SchedulerHelper.TIMEOUT, TimeUnit.SECONDS)
            .testSubscribe()

        outputStream.write(messageBytes)

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }
}
