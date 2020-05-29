package com.simprints.id.data.consent.longconsent

import kotlinx.coroutines.channels.ReceiveChannel


interface LongConsentRepository {

    suspend fun fetchLongConsent(language: String): String?

    suspend fun downloadLongConsent(languages: Array<String>): ReceiveChannel<Map<String, LongConsentFetchResult>>

    fun deleteLongConsents()
}
