package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

interface LongConsentRepository {

    suspend fun downloadLongConsentForLanguages(languages: Array<String>)

    suspend fun downloadLongConsentWithProgress(language: String): LiveData<Int>

    fun getLongConsentText(language: String): String

    fun deleteLongConsents()
}
