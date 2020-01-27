package com.simprints.fingerprintscanner.v2.scanner.errorhandler

import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.CaptureFingerprintResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class ResponseErrorHandler(val strategy: ResponseErrorHandlingStrategy,
                           private val timeOutScheduler: Scheduler = Schedulers.io()) {

    inline fun <reified T> handle(source: Single<T>): Single<T> {
        val timeOut = when (T::class.java) {
            Un20StateChangeEvent::class.java -> strategy.un20StateChangeEventTimeOut
            CaptureFingerprintResponse::class.java -> strategy.captureFingerprintResponseTimeOut
            else -> strategy.timeOutMs
        }
        return handle(source, timeOut, strategy.retryTimes)
    }

    fun <T> handle(source: Single<T>, timeOut: Long?, retryTimes: Long?): Single<T> =
        source
            .run {
                if (timeOut != null) timeout(timeOut, TimeUnit.MILLISECONDS, timeOutScheduler) else this
            }
            .run {
                if (retryTimes != null) retry(retryTimes) { it is TimeoutException } else this
            }
            .wrapExceptionsIfNecessary(timeOut, retryTimes)

    private fun <T> Single<T>.wrapExceptionsIfNecessary(timeOut: Long?, retryTimes: Long?) =
        onErrorResumeNext {
            when (it) {
                is TimeoutException -> Single.error(
                    IOException("Scanner did not respond after $timeOut milliseconds (with ${retryTimes
                        ?: "no"} retries)")
                )
                else -> Single.error(it)
            }
        }
}

inline fun <reified T> Single<T>.handleErrorsWith(handler: ResponseErrorHandler) =
    handler.handle(this)
