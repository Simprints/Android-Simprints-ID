package com.simprints.feature.dashboard.main.projectdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.domain.TokenizationAction
import com.simprints.infra.config.domain.models.TokenKeyType
import com.simprints.infra.config.tokenization.TokenizationManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProjectDetailsViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val tokenizationManager: TokenizationManager
) : ViewModel() {

    val projectCardStateLiveData: LiveData<DashboardProjectState>
        get() = _projectCardStateLiveData
    private val _projectCardStateLiveData = MutableLiveData<DashboardProjectState>()

    init {
        load()
    }

    fun load() = viewModelScope.launch {
        val state = try {
            val projectId = authStore.signedInProjectId
            val cachedProject = configManager.getProject(projectId)
            val recentUserActivity = recentUserActivityManager.getRecentUserActivity()
            val decryptedUserId = tokenizationManager.tryTokenize(
                value = recentUserActivity.lastUserUsed,
                tokenKeyType = TokenKeyType.AttendantId,
                action = TokenizationAction.Decrypt,
                project = cachedProject
            )
            DashboardProjectState(
                title = cachedProject.name,
                lastUser = decryptedUserId,
                lastScanner = recentUserActivity.lastScannerUsed,
                isLoaded = true
            )
        } catch (_: Throwable) {
            DashboardProjectState(isLoaded = false)
        }
        _projectCardStateLiveData.postValue(state)
    }
}
