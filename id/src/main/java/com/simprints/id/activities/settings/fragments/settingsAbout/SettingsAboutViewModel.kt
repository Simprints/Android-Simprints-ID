package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsAboutViewModel(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
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
        viewModelScope.launch(dispatcher) { signerManager.signOut() }
    }
}
