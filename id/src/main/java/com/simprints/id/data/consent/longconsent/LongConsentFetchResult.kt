package com.simprints.id.data.consent.longconsent

sealed class LongConsentFetchResult(open val language: String) {

    data class Succeed(override val language: String,
                       val consent: String) : LongConsentFetchResult(language)

    data class Failed(override val language: String,
                      val error: Throwable) : LongConsentFetchResult(language)

    /**
     * @param progress Progress from 0.0 to 1.0
     */
    data class Progress(override val language: String,
                        val progress: Float) : LongConsentFetchResult(language)
}
