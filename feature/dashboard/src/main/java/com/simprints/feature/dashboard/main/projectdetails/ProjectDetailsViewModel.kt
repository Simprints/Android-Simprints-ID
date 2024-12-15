package com.simprints.feature.dashboard.main.projectdetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ProjectDetailsViewModel @Inject constructor(
    private val configManager: ConfigManager,
    private val authStore: AuthStore,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val tokenizationProcessor: TokenizationProcessor,
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
            val decryptedUserId = when (val userId = recentUserActivity.lastUserUsed) {
                is TokenizableString.Raw -> userId
                is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                    encrypted = userId,
                    tokenKeyType = TokenKeyType.AttendantId,
                    project = cachedProject,
                )
            }
            DashboardProjectState(
                title = cachedProject.name,
                lastUser = decryptedUserId.value,
                lastScanner = recentUserActivity.lastScannerUsed,
                isLoaded = true,
            )
        } catch (_: Throwable) {
            DashboardProjectState(isLoaded = false)
        }
        _projectCardStateLiveData.postValue(state)
    }
}
