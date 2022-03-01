package com.simprints.id.data.consent.longconsent

sealed class LongConsentFetchResult(open val language: String) {

    data class Succeed(
        override val language: String,
        val consent: String
    ) : LongConsentFetchResult(language)

    data class Failed(
        override val language: String,
        val error: Throwable
    ) : LongConsentFetchResult(language)

    data class FailedBecauseBackendMaintenance(
        override val language: String,
        val error: Throwable,
        val estimatedOutage: Long? = null
    ) : LongConsentFetchResult(language)

    data class InProgress(override val language: String) : LongConsentFetchResult(language)
}
