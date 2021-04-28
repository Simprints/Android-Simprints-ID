package com.simprints.fingerprintscanner.v2.incoming.cypressota

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.domain.cypressota.CypressOtaResponse
import com.simprints.fingerprintscanner.v2.domain.cypressota.responses.ContinueResponse
import com.simprints.fingerprintscanner.v2.tools.helpers.SchedulerHelper
import com.simprints.fingerprintscanner.v2.tools.primitives.hexToByteArray
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import com.simprints.testtools.unit.reactive.testSubscribe
import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.concurrent.TimeUnit

class CypressOtaMessageInputStreamTest {

    private val cypressOtaResponseParser = CypressOtaResponseParser()
    private val cypressOtaMessageInputStream = CypressOtaMessageInputStream(cypressOtaResponseParser)

    @Suppress("Ignoring flaky tests introduced by Ridwan. These tests do not follow proper" +
        " RxJava testing methodology and fail frequently on the CI machines. They need to be " +
        "re-written when the RxJava is finally removed from the scanners SDK.")
    fun cypressOtaMessageInputStream_receiveCypressOtaResponse_correctlyForwardsResponse() {
        val messageBytes = "39".hexToByteArray()
        val expectedResponse = ContinueResponse()

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        cypressOtaMessageInputStream.connect(inputStream)

        val testSubscriber = cypressOtaMessageInputStream.receiveResponse<CypressOtaResponse>()
            .timeout(SchedulerHelper.TIMEOUT, TimeUnit.SECONDS).testSubscribe()

        outputStream.write(messageBytes)

        testSubscriber.awaitAndAssertSuccess()
        assertThat(testSubscriber.values().first()).isInstanceOf(expectedResponse::class.java)
    }
}
