package com.simprints.id.data.consent.longconsent

import androidx.lifecycle.LiveData

interface LongConsentRepository {

    val downloadProgressLiveData: LiveData<Int>

    val isDownloadSuccessfulLiveData: LiveData<Boolean>

    val longConsentTextLiveData: LiveData<String>

    suspend fun downloadLongConsentForLanguages(languages: Array<String>)

    suspend fun downloadLongConsentWithProgress()

    fun deleteLongConsents()

    fun setLanguage(language: String)
}
