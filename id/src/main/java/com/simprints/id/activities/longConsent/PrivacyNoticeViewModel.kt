package com.simprints.id.activities.longConsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.id.data.consent.longconsent.LongConsentFetchResult
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.infra.config.ConfigManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyNoticeViewModel @Inject constructor(
    private val longConsentRepository: LongConsentRepository,
    private val configManager: ConfigManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val privacyNoticeViewState = MutableLiveData<PrivacyNoticeViewState>()
    fun getPrivacyNoticeViewStateLiveData(): LiveData<PrivacyNoticeViewState> =
        privacyNoticeViewState

    fun retrievePrivacyNotice() = viewModelScope.launch(dispatcher) {
        val deviceConfiguration = configManager.getDeviceConfiguration()
        longConsentRepository.getLongConsentResultForLanguage(deviceConfiguration.language)
            .flowOn(dispatcher)
            .map { it.toPrivacyNoticeViewState() }
            .catch {
                it.printStackTrace()
                PrivacyNoticeViewState.ConsentNotAvailable(deviceConfiguration.language)
            }
            .collect { privacyNoticeViewState.postValue(it) }
    }


    private fun LongConsentFetchResult.toPrivacyNoticeViewState(): PrivacyNoticeViewState =
        when (this) {
            is LongConsentFetchResult.Succeed -> PrivacyNoticeViewState.ConsentAvailable(
                language,
                consent
            )
            is LongConsentFetchResult.Failed -> PrivacyNoticeViewState.ConsentNotAvailable(language)
            is LongConsentFetchResult.FailedBecauseBackendMaintenance ->
                PrivacyNoticeViewState.ConsentNotAvailableBecauseBackendMaintenance(
                    language,
                    estimatedOutage
                )
            is LongConsentFetchResult.InProgress -> PrivacyNoticeViewState.DownloadInProgress(
                language
            )
        }
}
