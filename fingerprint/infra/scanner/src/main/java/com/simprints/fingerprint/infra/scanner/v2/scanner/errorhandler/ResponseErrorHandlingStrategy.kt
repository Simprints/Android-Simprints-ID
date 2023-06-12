package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

data class ResponseErrorHandlingStrategy(
    val retryTimes: Long? = null,
    val generalTimeOutMs: Long? = null,
    val setUn20ResponseTimeOut: Long? = null,
    val un20StateChangeEventTimeOut: Long? = null,
    val captureFingerprintResponseTimeOut: Long? = null,
    val getImageResponseTimeOut: Long? = null
) {
    companion object {
        val DEFAULT = ResponseErrorHandlingStrategy(
            retryTimes = null,
            generalTimeOutMs = 15000,
            setUn20ResponseTimeOut = 15000,
            un20StateChangeEventTimeOut = 15000,
            captureFingerprintResponseTimeOut = 5000,
            getImageResponseTimeOut = 15000
        )

        val NONE = ResponseErrorHandlingStrategy(
            retryTimes = null,
            generalTimeOutMs = null,
            un20StateChangeEventTimeOut = null,
            captureFingerprintResponseTimeOut = null,
            getImageResponseTimeOut = null
        )
    }
}
