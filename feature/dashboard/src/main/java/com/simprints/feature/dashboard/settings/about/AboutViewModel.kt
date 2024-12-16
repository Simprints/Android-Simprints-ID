package com.simprints.feature.dashboard.settings.about

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.livedata.LiveDataEvent
import com.simprints.core.livedata.LiveDataEventWithContent
import com.simprints.core.livedata.send
import com.simprints.feature.dashboard.logout.usecase.LogoutUseCase
import com.simprints.feature.troubleshooting.AutoResettingClickCounter
import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.config.store.models.SettingsPasswordConfig
import com.simprints.infra.config.store.models.canSyncDataToSimprints
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.eventsync.EventSyncManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AboutViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val logoutUseCase: LogoutUseCase,
    private val eventSyncManager: EventSyncManager,
    private val recentUserActivityManager: RecentUserActivityManager,
) : ViewModel() {
    val syncAndSearchConfig: LiveData<SyncAndSearchConfig>
        get() = _syncAndSearchConfig
    private val _syncAndSearchConfig = MutableLiveData<SyncAndSearchConfig>()

    val modalities: LiveData<List<GeneralConfiguration.Modality>>
        get() = _modalities
    private val _modalities = MutableLiveData<List<GeneralConfiguration.Modality>>()

    val recentUserActivity: LiveData<RecentUserActivity>
        get() = _recentUserActivity
    private val _recentUserActivity = MutableLiveData<RecentUserActivity>()

    val settingsLocked: LiveData<SettingsPasswordConfig>
        get() = _settingsLocked
    private val _settingsLocked =
        MutableLiveData<SettingsPasswordConfig>(SettingsPasswordConfig.NotSet)

    val logoutDestinationEvent: LiveData<LiveDataEventWithContent<LogoutDestination>>
        get() = _logoutDestinationEvent
    private val _logoutDestinationEvent =
        MutableLiveData<LiveDataEventWithContent<LogoutDestination>>()

    val openTroubleshooting: LiveData<LiveDataEvent>
        get() = _openTroubleshooting
    private val _openTroubleshooting = MutableLiveData<LiveDataEvent>()

    private var troubleshootingClickCounter = AutoResettingClickCounter()

    init {
        load()
    }

    fun unlockSettings() {
        _settingsLocked.postValue(SettingsPasswordConfig.Unlocked)
    }

    fun processLogoutRequest() {
        viewModelScope.launch {
            val logoutDestination =
                when (canSyncDataToSimprints() && hasEventsToUpload()) {
                    true -> LogoutDestination.LogoutDataSyncScreen
                    false -> {
                        logoutUseCase()
                        LogoutDestination.LoginScreen
                    }
                }
            _logoutDestinationEvent.send(logoutDestination)
        }
    }

    private suspend fun hasEventsToUpload(): Boolean = eventSyncManager.countEventsToUpload(type = null).first() > 0

    private suspend fun canSyncDataToSimprints(): Boolean = configManager.getProjectConfiguration().canSyncDataToSimprints()

    private fun load() = viewModelScope.launch {
        val configuration = configManager.getProjectConfiguration()
        val syncAndSearchConfig = SyncAndSearchConfig(
            configuration.synchronization.down.partitionType.name,
            configuration.identification.poolType.name,
        )
        _syncAndSearchConfig.postValue(syncAndSearchConfig)
        _modalities.postValue(configuration.general.modalities)
        _recentUserActivity.postValue(recentUserActivityManager.getRecentUserActivity())
        _settingsLocked.postValue(configuration.general.settingsPassword)
    }

    fun troubleshootingClick() {
        if (troubleshootingClickCounter.handleClick(viewModelScope)) {
            _openTroubleshooting.send()
        }
    }
}
