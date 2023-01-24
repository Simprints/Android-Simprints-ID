package com.simprints.id.activities.longConsent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.login.LoginManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyNoticeViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
) : ViewModel() {

    private val privacyNoticeViewState = MutableLiveData<PrivacyNoticeState>()
    fun getPrivacyNoticeViewStateLiveData(): LiveData<PrivacyNoticeState> =
        privacyNoticeViewState

    fun retrievePrivacyNotice() = viewModelScope.launch {
        val deviceConfiguration = configManager.getDeviceConfiguration()
        configManager.getPrivacyNotice(
            loginManager.getSignedInProjectIdOrEmpty(),
            deviceConfiguration.language
        )
            .map { it.toPrivacyNoticeViewState() }
            .catch {
                it.printStackTrace()
                PrivacyNoticeState.ConsentNotAvailable(deviceConfiguration.language)
            }
            .collect { privacyNoticeViewState.postValue(it) }
    }


    private fun PrivacyNoticeResult.toPrivacyNoticeViewState(): PrivacyNoticeState =
        when (this) {
            is PrivacyNoticeResult.Succeed -> PrivacyNoticeState.ConsentAvailable(
                language,
                consent
            )
            is PrivacyNoticeResult.Failed -> PrivacyNoticeState.ConsentNotAvailable(language)
            is PrivacyNoticeResult.FailedBecauseBackendMaintenance ->
                PrivacyNoticeState.ConsentNotAvailableBecauseBackendMaintenance(
                    language,
                    estimatedOutage
                )
            is PrivacyNoticeResult.InProgress -> PrivacyNoticeState.DownloadInProgress(
                language
            )
        }
}
