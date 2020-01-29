package com.simprints.fingerprintscanner.v2.scanner.errorhandler

open class ResponseErrorHandlingStrategy(
    val timeOutMs: Long? = null,
    val retryTimes: Long? = null,
    val un20StateChangeEventTimeOut: Long? = null,
    val captureFingerprintResponseTimeOut: Long? = null,
    val getImageResponseTimeOut: Long? = null
) {
    object Default : ResponseErrorHandlingStrategy(
        timeOutMs = 2000,
        retryTimes = null,
        un20StateChangeEventTimeOut = 5000,
        captureFingerprintResponseTimeOut = 5000,
        getImageResponseTimeOut = null
    )

    object None : ResponseErrorHandlingStrategy(
        timeOutMs = null,
        retryTimes = null,
        un20StateChangeEventTimeOut = null,
        captureFingerprintResponseTimeOut = null,
        getImageResponseTimeOut = null
    )
}
