package com.simprints.feature.consent.screens.privacy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.send
import com.simprints.core.tools.utils.TimeUtils
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.PrivacyNoticeResult
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Failed
import com.simprints.infra.config.store.models.PrivacyNoticeResult.FailedBecauseBackendMaintenance
import com.simprints.infra.config.store.models.PrivacyNoticeResult.InProgress
import com.simprints.infra.config.store.models.PrivacyNoticeResult.Succeed
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class PrivacyNoticeViewModel @Inject constructor(
    private val connectivityTracker: ConnectivityTracker,
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
) : ViewModel() {

    private val _viewState = MutableLiveData<PrivacyNoticeState>()
    val viewState: LiveData<PrivacyNoticeState>
        get() = _viewState

    private val _showOffline = MutableLiveData<LiveDataEvent>()
    val showOffline: LiveData<LiveDataEvent>
        get() = _showOffline

    fun downloadPressed() {
        if (connectivityTracker.isConnected()) {
            retrievePrivacyNotice()
        } else {
            _showOffline.send()
        }
    }

    fun retrievePrivacyNotice() = viewModelScope.launch {
        val deviceConfiguration = configManager.getDeviceConfiguration()
        configManager.getPrivacyNotice(
            authStore.signedInProjectId,
            deviceConfiguration.language
        )
            .map { it.toPrivacyNoticeViewState() }
            .catch {
                it.printStackTrace()
                PrivacyNoticeState.ConsentNotAvailable
            }
            .collect { _viewState.postValue(it) }
    }

    private fun PrivacyNoticeResult.toPrivacyNoticeViewState(): PrivacyNoticeState = when (this) {
        is Succeed -> PrivacyNoticeState.ConsentAvailable(consent)
        is InProgress -> PrivacyNoticeState.DownloadInProgress
        is Failed -> PrivacyNoticeState.ConsentNotAvailable
        is FailedBecauseBackendMaintenance -> PrivacyNoticeState.BackendMaintenance(
            estimatedOutage?.let { TimeUtils.getFormattedEstimatedOutage(it) },
        )
    }
}
