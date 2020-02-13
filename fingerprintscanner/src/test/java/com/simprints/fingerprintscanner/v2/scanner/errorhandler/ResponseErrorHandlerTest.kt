package com.simprints.fingerprintscanner.v2.scanner.errorhandler

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprintscanner.v2.exceptions.parsing.InvalidMessageException
import com.simprints.testtools.common.syntax.awaitAndAssertSuccess
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ResponseErrorHandlerTest {

    @Test
    fun handleError_timesOutThenSucceeds_retriesCorrectNumberOfTimesAndSucceeds() {
        val currentTry = AtomicInteger(0)
        val shouldSucceedOnTry = 4
        val successValue = "success!"

        val testScheduler = TestScheduler()

        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy(
            generalTimeOutMs = 500, retryTimes = 3
        ), testScheduler)

        val testSubscriber =
            Single.defer {
                Single.just(successValue)
                    .run {
                        if (currentTry.incrementAndGet() != shouldSucceedOnTry) {
                            delay(2000, TimeUnit.MILLISECONDS, testScheduler)
                        } else {
                            this
                        }
                    }
            }
                .handleErrorsWith(responseErrorHandler).test()

        testScheduler.advanceTimeBy(10000, TimeUnit.MILLISECONDS)

        testSubscriber.awaitAndAssertSuccess()
        testSubscriber.assertValue(successValue)
        assertThat(currentTry.get()).isEqualTo(shouldSucceedOnTry)
    }

    @Test
    fun handleError_timesOutForAllRetries_failsDespiteRetries() {
        val currentTry = AtomicInteger(0)
        val shouldSucceedOnTry = 4
        val successValue = "success!"

        val testScheduler = TestScheduler()

        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy(
            generalTimeOutMs = 500, retryTimes = 2
        ), testScheduler)

        val testSubscriber =
            Single.defer {
                Single.just(successValue)
                    .run {
                        if (currentTry.incrementAndGet() != shouldSucceedOnTry) {
                            delay(2000, TimeUnit.MILLISECONDS, testScheduler)
                        } else {
                            this
                        }
                    }
            }
                .handleErrorsWith(responseErrorHandler).test()

        testScheduler.advanceTimeBy(10000, TimeUnit.MILLISECONDS)

        testSubscriber.await()
        testSubscriber.assertError(IOException::class.java)
    }

    @Test
    fun handleError_propagatesNonTimeoutErrors() {
        val thrownException = InvalidMessageException()

        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)

        val testSubscriber = Single
            .error<String>(thrownException)
            .handleErrorsWith(responseErrorHandler)
            .test()

        testSubscriber.await()
        testSubscriber.assertError(thrownException)
    }

    @Test
    fun handleError_givenNoneStrategy_doesNotTimeoutOrRetryAndWaitsUntilSucceeds() {
        val currentTry = AtomicInteger(0)
        val shouldSucceedOnTry = 4
        val successValue = "success!"

        val testScheduler = TestScheduler()

        val responseErrorHandler = ResponseErrorHandler(ResponseErrorHandlingStrategy.NONE, testScheduler)

        val testSubscriber =
            Single.defer {
                Single.just(successValue)
                    .run {
                        if (currentTry.incrementAndGet() != shouldSucceedOnTry) {
                            delay(2000, TimeUnit.MILLISECONDS, testScheduler)
                        } else {
                            this
                        }
                    }
            }
                .handleErrorsWith(responseErrorHandler).test()

        testScheduler.advanceTimeBy(10000, TimeUnit.MILLISECONDS)

        testSubscriber.await()
        testSubscriber.assertValue(successValue)
        assertThat(currentTry.get()).isEqualTo(1)
    }
}
