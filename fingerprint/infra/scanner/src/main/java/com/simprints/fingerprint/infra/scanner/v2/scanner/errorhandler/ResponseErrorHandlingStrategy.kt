package com.simprints.fingerprint.infra.scanner.v2.scanner.errorhandler

data class ResponseErrorHandlingStrategy(
    val generalTimeOutMs: Long,
    val setUn20ResponseTimeOut: Long,
    val un20StateChangeEventTimeOut: Long,
    val captureFingerprintResponseTimeOut: Long,
    val getImageResponseTimeOut: Long
) {
    companion object {
        val DEFAULT = ResponseErrorHandlingStrategy(
            generalTimeOutMs = 15000,
            setUn20ResponseTimeOut = 15000,
            un20StateChangeEventTimeOut = 15000,
            captureFingerprintResponseTimeOut = 5000,
            getImageResponseTimeOut = 15000
        )
    }
}
