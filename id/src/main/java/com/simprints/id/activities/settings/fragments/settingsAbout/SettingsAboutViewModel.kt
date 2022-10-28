package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsAboutViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val recentUserActivityManager: RecentUserActivityManager,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val configuration = MutableLiveData<ProjectConfiguration>()
    val recentUserActivity = MutableLiveData<RecentUserActivity>()

    init {
        viewModelScope.launch(dispatcher) {
            configuration.postValue(configManager.getProjectConfiguration())
            recentUserActivity.postValue(recentUserActivityManager.getRecentUserActivity())
        }
    }

    fun logout() {
        viewModelScope.launch { signerManager.signOut() }
    }
}
