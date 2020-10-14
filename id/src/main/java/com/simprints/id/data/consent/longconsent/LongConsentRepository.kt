package com.simprints.id.data.consent.longconsent

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow


interface LongConsentRepository {

    fun getLongConsentForLanguage(language: String): Flow<LongConsentFetchResult>

    suspend fun fetchLongConsent(language: String): String?

    suspend fun downloadLongConsent(languages: Array<String>): ReceiveChannel<Map<String, LongConsentFetchResult>>

    fun deleteLongConsents()
}
