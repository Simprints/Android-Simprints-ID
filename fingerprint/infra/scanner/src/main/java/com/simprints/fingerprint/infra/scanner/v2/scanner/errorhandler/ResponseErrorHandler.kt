package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler


import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetImageResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.SetUn20OnResponse
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException

class ResponseErrorHandler(
    val strategy: ResponseErrorHandlingStrategy
) {

    /**
     * Adds a timeout for incoming messages
     */

    suspend inline fun < reified T> handle(crossinline block: suspend () -> T): T {
        val timeOut = when (T::class.java) {
            SetUn20OnResponse::class.java -> strategy.setUn20ResponseTimeOut
            Un20StateChangeEvent::class.java -> strategy.un20StateChangeEventTimeOut
            CaptureFingerprintResponse::class.java -> strategy.captureFingerprintResponseTimeOut
            GetImageResponse::class.java -> strategy.getImageResponseTimeOut
            else -> strategy.generalTimeOutMs
        }
        return handle(block, timeOut)
    }

    suspend inline fun <T> handle(
        crossinline block: suspend () -> T,
        timeoutDelay: Long,
    ): T = try {
        withTimeout(timeoutDelay) {
            block()
        }
    } catch (_: TimeoutCancellationException) {
        throw IOException("Scanner did not respond after $timeoutDelay milliseconds")
    }
}



