package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.LiveData

interface LongConsentRepository {

    val downloadProgress: LiveData<Int>

    val isDownloadSuccessful: LiveData<Boolean>

    val longConsentText: LiveData<String>

    suspend fun downloadLongConsentForLanguages(languages: Array<String>)

    suspend fun downloadLongConsentWithProgress()

    fun deleteLongConsents()

    fun setLanguage(language: String)
}
