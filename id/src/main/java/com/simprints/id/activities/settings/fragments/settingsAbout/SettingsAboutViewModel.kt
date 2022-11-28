package com.simprints.id.activities.settings.fragments.settingsAbout

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.DispatcherIO
import com.simprints.core.ExternalScope
import com.simprints.id.secure.SignerManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.ProjectConfiguration
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.recent.user.activity.domain.RecentUserActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsAboutViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val signerManager: SignerManager,
    private val recentUserActivityManager: RecentUserActivityManager,
    @ExternalScope private val externalScope: CoroutineScope,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val configuration = MutableLiveData<ProjectConfiguration>()
    val recentUserActivity = MutableLiveData<RecentUserActivity>()

    init {
        viewModelScope.launch(dispatcher) {
            //Adding "this" in front of livedata to fix NullSafeMutableLiveData lint issues
            this@SettingsAboutViewModel.configuration.postValue(configManager.getProjectConfiguration())
            this@SettingsAboutViewModel.recentUserActivity.postValue(recentUserActivityManager.getRecentUserActivity())
        }
    }

    fun logout() {
        externalScope.launch { signerManager.signOut() }
    }
}
