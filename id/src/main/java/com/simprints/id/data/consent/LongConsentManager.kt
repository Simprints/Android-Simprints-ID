package com.simprints.id.data.consent

import kotlinx.coroutines.flow.Flow

interface LongConsentManager {

    suspend fun downloadAllLongConsents(languages: Array<String>)

    suspend fun downloadLongConsentWithProgress(language: String): Flow<Int>

    fun checkIfLongConsentExistsInLocal(language: String): Boolean

    fun getLongConsentText(language: String): String

    fun deleteLongConsents()
}
