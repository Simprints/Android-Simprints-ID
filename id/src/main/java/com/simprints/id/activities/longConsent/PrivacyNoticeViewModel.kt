package com.simprints.id.activities.longConsent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import kotlinx.coroutines.launch

class PrivacyNoticeViewModel(private val longConsentRepository: LongConsentRepository,
                             private val language: String): ViewModel() {

    val isDownloadSuccessfulLiveData = longConsentRepository.isDownloadSuccessfulLiveData
    val downloadProgressLiveData = longConsentRepository.downloadProgressLiveData

    val longConsentTextLiveData = longConsentRepository.longConsentTextLiveData

    fun start() {
        longConsentRepository.setLanguage(language)
    }

    fun downloadLongConsent() {
        viewModelScope.launch {
            longConsentRepository.downloadLongConsentWithProgress()
        }
    }
}
