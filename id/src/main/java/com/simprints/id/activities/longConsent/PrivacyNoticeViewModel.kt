package com.simprints.id.activities.longConsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import kotlinx.coroutines.launch


class PrivacyNoticeViewModel(private val longConsentRepository: LongConsentRepository,
                             private val language: String) : ViewModel() {

    private val privateNoticeViewState = MutableLiveData<PrivacyNoticeViewState>()
    val privateNoticeViewStateLiveData: LiveData<PrivacyNoticeViewState> = privateNoticeViewState

    init {
        viewModelScope.launch {
            val consent = longConsentRepository.fetchLongConsent(language)
            consent?.let {
                privateNoticeViewState.postValue(PrivacyNoticeViewState.ConsentAvailable(language, it))
            }
        }
    }

    fun downloadLongConsent() {
        viewModelScope.launch {
            try {
                val downloadProgressChannel = longConsentRepository.downloadLongConsent(arrayOf(language))
                for (downloadState in downloadProgressChannel) {
                    when (val languageDownloadState = downloadState[language]) {
                        is LongConsentFetchResult.Progress -> PrivacyNoticeViewState.DownloadInProgress(language, (languageDownloadState.progress * 100).toInt())
                        is LongConsentFetchResult.Succeed -> PrivacyNoticeViewState.ConsentAvailable(language, languageDownloadState.consent)
                        is LongConsentFetchResult.Failed -> PrivacyNoticeViewState.ConsentNotAvailable(language)
                        else -> null
                    }.also {
                        it?.let { viewState ->
                            privateNoticeViewState.postValue(viewState)
                        }
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                PrivacyNoticeViewState.ConsentNotAvailable(language)
            }
        }
    }
}
