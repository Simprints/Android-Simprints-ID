package com.simprints.feature.dashboard.main.projectdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProjectDetailsViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
    private val recentUserActivityManager: RecentUserActivityManager,
) : ViewModel() {

    val projectCardStateLiveData: LiveData<DashboardProjectState>
        get() = _projectCardStateLiveData
    private val _projectCardStateLiveData = MutableLiveData<DashboardProjectState>()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        val projectId = loginManager.getSignedInProjectIdOrEmpty()
        val cachedProject = configManager.getProject(projectId)
        val recentUserActivity = recentUserActivityManager.getRecentUserActivity()

        val state = DashboardProjectState(
            cachedProject.name,
            recentUserActivity.lastUserUsed,
            recentUserActivity.lastScannerUsed
        )
        _projectCardStateLiveData.postValue(state)
    }
}
