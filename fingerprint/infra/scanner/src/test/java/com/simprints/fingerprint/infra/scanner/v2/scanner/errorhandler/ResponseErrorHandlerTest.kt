package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.OperationResultCode
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import com.simprints.fingerprint.infra.scanner.v2.exceptions.parsing.InvalidMessageException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class ResponseErrorHandlerTest {

    @Test
    fun handle_succeeds() = runTest {
        val successValue = Un20StateChangeEvent(DigitalValue.TRUE)
        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)
        val result = responseErrorHandler.handle { successValue }

        assertThat(result).isEqualTo(successValue)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test(expected = IOException::class)
    fun handle_fails() = runTest {
        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)
        val delayTimeMilliSeconds = ResponseErrorHandlingStrategy.DEFAULT.generalTimeOutMs + 1000
        responseErrorHandler.handle {
            delay(delayTimeMilliSeconds)
            SetUn20OnResponse(OperationResultCode.OK)
        }
        advanceTimeBy(delayTimeMilliSeconds)
    }

    @Test(expected = InvalidMessageException::class)
    fun handle_propagatesNonTimeoutErrors() = runTest {
        val thrownException = InvalidMessageException()
        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)
        responseErrorHandler.handle<String> {
            throw thrownException
        }
    }
}
