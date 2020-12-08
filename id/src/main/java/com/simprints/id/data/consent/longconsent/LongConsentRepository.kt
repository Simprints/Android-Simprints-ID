package com.simprints.id.data.consent.longconsent

import kotlinx.coroutines.flow.Flow

interface LongConsentRepository {

    fun getLongConsentResultForLanguage(language: String): Flow<LongConsentFetchResult>

    fun deleteLongConsents()
}
