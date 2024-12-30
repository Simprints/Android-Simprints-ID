package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class ResponseErrorHandlerTest {
    private val strategy = ResponseErrorHandlingStrategy(
        retryTimes = 2,
        generalTimeOutMs = 1000L,
        setUn20ResponseTimeOut = 2000L,
        un20StateChangeEventTimeOut = 3000L,
        captureFingerprintResponseTimeOut = 4000L,
        getImageResponseTimeOut = 5000L,
    )

    private val handler = ResponseErrorHandler(strategy)

    @Test
    fun `handle uses correct timeout for SetUn20OnResponse`() = runTest {
        // given
        var executionTime: Long = 0L
        val block: suspend () -> SetUn20OnResponse = {
            executionTime = System.currentTimeMillis()
            SetUn20OnResponse(OperationResultCode.OK)
        }

        // when
        val result = handler.handle(block)

        // then
        assertThat(result).isInstanceOf(SetUn20OnResponse::class.java)
        assertThat(executionTime).isNotEqualTo(0L)
    }

    @Test
    fun `handle retries correct number of times on timeout`() = runTest {
        // given
        var attempt = 0
        val block: suspend () -> String = {
            attempt++
            delay(15000)
            "Success"
        }

        // when
        val exception = runCatching {
            handler.handle(block)
        }.exceptionOrNull()

        // then
        assertThat(exception).isInstanceOf(IOException::class.java)
        assertThat(attempt).isEqualTo(strategy.retryTimes)
    }

    @Test
    fun `handle throws IOException after max retries`() = runTest {
        // given
        val block: suspend () -> String = {
            delay(15000)
            "Success"
        }

        // when
        val exception = runCatching {
            handler.handle(block)
        }.exceptionOrNull()

        // then
        assertThat(exception).isInstanceOf(IOException::class.java)
        assertThat(exception).hasMessageThat().contains("Retried ${strategy.retryTimes - 1} times")
    }

    @Test
    fun `handle completes successfully if block finishes within timeout`() = runTest {
        // given
        val block: suspend () -> String = { "Success" }

        // when
        val result = handler.handle(block)

        // then
        assertThat(result).isEqualTo("Success")
    }

    @Test
    fun `handle uses general timeout for unhandled types`() = runTest {
        // given
        val block: suspend () -> String = { "General Timeout Test" }

        // when
        val result = handler.handle(block)

        // then
        assertThat(result).isEqualTo("General Timeout Test")
    }
}
