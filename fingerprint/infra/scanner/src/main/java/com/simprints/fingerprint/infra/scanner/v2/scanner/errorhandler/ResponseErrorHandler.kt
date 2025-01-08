package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.fingerprint.infra.scanner.v1.ScannerUtils.log
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import javax.inject.Inject

class ResponseErrorHandler @Inject constructor(
    val strategy: ResponseErrorHandlingStrategy,
) {
    /**
     * Handles the execution of a suspending block with a timeout based on the response type.
     *
     * This function determines the appropriate timeout for the given response type `T` and executes
     * the provided suspending block within that timeout. If the block does not complete within the
     * specified timeout, an `IOException` is thrown.
     *
     * @param T The type of the response.
     * @param block The suspending block to be executed.
     * @return The result of the suspending block.
     * @throws IOException If the block does not complete within the specified timeout.
     */
    @ExcludedFromGeneratedTestCoverageReports(
        "This fun is already tested in ResponseErrorHandlerTest. " +
            "but it is not covered in the test coverage report as it is inline functions.",
    )
    suspend inline fun <reified T> handle(crossinline block: suspend () -> T): T {
        val timeOut = when (T::class.java) {
            SetUn20OnResponse::class.java -> strategy.setUn20ResponseTimeOut
            Un20StateChangeEvent::class.java -> strategy.un20StateChangeEventTimeOut
            CaptureFingerprintResponse::class.java -> strategy.captureFingerprintResponseTimeOut
            GetImageResponse::class.java -> strategy.getImageResponseTimeOut
            else -> strategy.generalTimeOutMs
        }
        return handle(block, timeOut, strategy.retryTimes)
    }

    @ExcludedFromGeneratedTestCoverageReports(
        "This function is already tested in ResponseErrorHandlerTest, but it does not appear in the test coverage report because it is an inline function",
    )
    suspend inline fun <reified T> handle(
        crossinline block: suspend () -> T,
        timeoutDelay: Long,
        retryTimes: Int,
    ): T {
        repeat(retryTimes) { attempt ->
            try {
                return withTimeout(timeoutDelay) { block() }
            } catch (_: TimeoutCancellationException) {
                log("Disconnected after $timeoutDelay ms for ${T::class.simpleName}. Retried $attempt times.")
                if (attempt == retryTimes - 1) {
                    throw IOException(
                        "Disconnected after $timeoutDelay ms for ${T::class.simpleName}. Retried $attempt times.",
                    )
                }
            }
        }
        error("Unreachable code") // This is just a safeguard and should not be reached.
    }
}
