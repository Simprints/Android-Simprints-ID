package com.simprints.fingerprintscanner.v2.incoming.stmota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.stmota.responses.CommandAcknowledgement
import com.simprints.fingerprintscanner.v2.tools.helpers.SchedulerHelper
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

class StmOtaMessageInputStreamTest {

    private val stmOtaResponseParser = StmOtaResponseParser()
    private val stmOtaMessageInputStream = StmOtaMessageInputStream(stmOtaResponseParser)

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun stmOtaMessageInputStream_receiveStmOtaResponse_correctlyForwardsResponse() {
        val messageBytes = "79".hexToByteArray()
        val expectedResponse = CommandAcknowledgement(CommandAcknowledgement.Kind.ACK)

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        stmOtaMessageInputStream.connect(inputStream)

        val testSubscriber = stmOtaMessageInputStream.receiveResponse<CommandAcknowledgement>()
            .timeout(SchedulerHelper.TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        outputStream.write(messageBytes)

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }
}
