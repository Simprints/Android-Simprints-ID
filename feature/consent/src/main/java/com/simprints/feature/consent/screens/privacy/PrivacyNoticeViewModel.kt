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
import com.simprints.infra.logging.Simber
import com.simprints.infra.network.ConnectivityTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
        val projectId = authStore.signedInProjectId
        val deviceLanguage = configManager.getDeviceConfiguration().language
        val defaultLanguage = configManager.getProjectConfiguration().general.defaultLanguage

        attemptDownloadingLanguage(projectId, deviceLanguage)
            .flatMapLatest {
                if (it is PrivacyNoticeState.ConsentNotAvailable) {
                    Simber.i("Privacy notice in ($deviceLanguage) not available")
                    attemptDownloadingLanguage(projectId, defaultLanguage)
                } else {
                    flowOf(it)
                }
            }.catch {
                Simber.i("Notice download failed", it)
                emit(PrivacyNoticeState.ConsentNotAvailable)
            }.collect {
                _viewState.postValue(it)
            }
    }

    private fun attemptDownloadingLanguage(
        projectId: String,
        deviceLanguage: String,
    ): Flow<PrivacyNoticeState> = configManager.getPrivacyNotice(projectId, deviceLanguage).map { it.toPrivacyNoticeViewState() }

    private fun PrivacyNoticeResult.toPrivacyNoticeViewState(): PrivacyNoticeState = when (this) {
        is Succeed -> PrivacyNoticeState.ConsentAvailable(consent)
        is InProgress -> PrivacyNoticeState.DownloadInProgress
        is Failed -> PrivacyNoticeState.ConsentNotAvailable
        is FailedBecauseBackendMaintenance -> PrivacyNoticeState.BackendMaintenance(
            estimatedOutage?.let { TimeUtils.getFormattedEstimatedOutage(it) },
        )
    }
}
