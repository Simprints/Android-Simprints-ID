package com.simprints.feature.dashboard.privacynotices

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.Available
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.DownloadInProgress
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.NotAvailable
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.NotAvailableBecauseBackendMaintenance
import com.simprints.feature.dashboard.privacynotices.PrivacyNoticeState.NotConnectedToInternet
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.PrivacyNoticeResult
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.Failed
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.FailedBecauseBackendMaintenance
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.InProgress
import com.simprints.infra.config.domain.models.PrivacyNoticeResult.Succeed
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PrivacyNoticesViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
    private val connectivityTracker: ConnectivityTracker,
) : ViewModel() {

    val privacyNoticeState: LiveData<PrivacyNoticeState>
        get() = _privacyNoticeState
    private val _privacyNoticeState = MutableLiveData<PrivacyNoticeState>()

    fun fetchPrivacyNotice() = viewModelScope.launch {
        val deviceConfiguration = configManager.getDeviceConfiguration()
        configManager.getPrivacyNotice(
            loginManager.getSignedInProjectIdOrEmpty(),
            deviceConfiguration.language
        )
            .map { it.toState(deviceConfiguration.language) }
            .collect { _privacyNoticeState.postValue(it) }
    }

    private fun PrivacyNoticeResult.toState(language: String): PrivacyNoticeState =
        when (this) {
            is Failed -> if (connectivityTracker.observeIsConnected().value != false) NotAvailable(language)
            else NotConnectedToInternet(language)
            is FailedBecauseBackendMaintenance -> NotAvailableBecauseBackendMaintenance(
                language,
                estimatedOutage
            )
            is InProgress -> DownloadInProgress(language)
            is Succeed -> Available(language, consent)
        }
}
