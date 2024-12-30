package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

data class ResponseErrorHandlingStrategy(
    val retryTimes: Int = 0,
    val generalTimeOutMs: Long,
    val setUn20ResponseTimeOut: Long,
    val un20StateChangeEventTimeOut: Long,
    val captureFingerprintResponseTimeOut: Long,
    val getImageResponseTimeOut: Long,
) {
    companion object {
        val DEFAULT = ResponseErrorHandlingStrategy(
            retryTimes = 2,
            generalTimeOutMs = 5000,
            setUn20ResponseTimeOut = 5000,
            un20StateChangeEventTimeOut = 5000,
            captureFingerprintResponseTimeOut = 5000,
            getImageResponseTimeOut = 7000,
        )
    }
}
