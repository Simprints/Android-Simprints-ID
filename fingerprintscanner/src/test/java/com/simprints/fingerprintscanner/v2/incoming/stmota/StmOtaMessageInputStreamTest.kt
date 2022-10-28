package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.stmota.StmOtaResponse
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.tools.helpers.SchedulerHelper
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.fingerprintscanner.v2.tools.reactive.toFlowable
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

class StmOtaMessageInputStreamTest {

    private val stmOtaResponseParser = StmOtaResponseParser()
    private val stmOtaMessageInputStream = StmOtaMessageInputStream(stmOtaResponseParser)

    @Test
    fun `test disconnect disposes the flowable stream`(){
        //Given
        val flowableDisposable = mockk<Disposable>(relaxed = true)

        val stmResponseFlowable: Flowable<StmOtaResponse> = mockk {
            every { subscribeOn(any()) } returns this
            every { publish() } returns mockk {
                every { connect() } returns flowableDisposable
            }
        }

        val flowable: Flowable<ByteArray> = mockk{
            every { map(any<Function<ByteArray, StmOtaResponse>>()) } returns stmResponseFlowable
        }

        //When
        stmOtaMessageInputStream.connect(flowable)
        stmOtaMessageInputStream.disconnect()

        //Then
        verify { flowableDisposable.dispose() }
    }

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun stmOtaMessageInputStream_receiveStmOtaResponse_correctlyForwardsResponse() {
        val messageBytes = "79".hexToByteArray()
        val expectedResponse = CommandAcknowledgement(CommandAcknowledgement.Kind.ACK)

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        stmOtaMessageInputStream.connect(inputStream.toFlowable())

        val testSubscriber = stmOtaMessageInputStream.receiveResponse<CommandAcknowledgement>()
            .timeout(SchedulerHelper.TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        outputStream.write(messageBytes)

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }
}
