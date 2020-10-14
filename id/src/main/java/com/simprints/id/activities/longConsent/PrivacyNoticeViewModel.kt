package com.simprints.id.activities.longConsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class PrivacyNoticeViewModel(
    private val longConsentRepository: LongConsentRepository,
    private val language: String,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val privacyNoticeViewState = MutableLiveData<PrivacyNoticeViewState>()
    fun getPrivacyNoticeViewStateLiveData(): LiveData<PrivacyNoticeViewState> = privacyNoticeViewState

    fun retrievePrivacyNotice() = viewModelScope.launch {
        longConsentRepository.getLongConsentForLanguage(language)
            .flowOn(dispatcherProvider.io())
            .map { it.toPrivacyNoticeViewState() }
            .catch {
                it.printStackTrace()
                PrivacyNoticeViewState.ConsentNotAvailable(language)
            }
            .collect { privacyNoticeViewState.value = it }
    }


    private fun LongConsentFetchResult.toPrivacyNoticeViewState(): PrivacyNoticeViewState =
        when (this) {
            is LongConsentFetchResult.Succeed -> PrivacyNoticeViewState.ConsentAvailable(
                language,
                consent
            )
            is LongConsentFetchResult.Failed -> PrivacyNoticeViewState.ConsentNotAvailable(language)
            is LongConsentFetchResult.Progress -> PrivacyNoticeViewState.DownloadInProgress(
                language,
                (progress * 100).toInt()
            )
        }
}
