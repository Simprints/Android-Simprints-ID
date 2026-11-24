package com.simprints.feature.dashboard.main.projectdetails

import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.logging.Simber
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
internal class ProjectDetailsViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val tokenizationProcessor: TokenizationProcessor,
) : BaseMviViewModel<ProjectDetailsUiState, ProjectDetailsUiAction, ProjectDetailsUiEffect>(ProjectDetailsUiState()) {
    override suspend fun handleAction(action: ProjectDetailsUiAction) {
        when (action) {
            ProjectDetailsUiAction.Load -> load()
        }
    }

    private suspend fun load() {
        val state = try {
            configRepository.getProject()?.let { cachedProject ->
                val recentUserActivity = recentUserActivityManager.getRecentUserActivity()
                val decryptedUserId = when (val userId = recentUserActivity.lastUserUsed) {
                    is TokenizableString.Raw -> userId

                    is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                        encrypted = userId,
                        tokenKeyType = TokenKeyType.AttendantId,
                        project = cachedProject,
                    )
                }
                ProjectDetailsUiState(
                    isLoaded = true,
                    title = cachedProject.name,
                    lastUser = decryptedUserId.value,
                    lastScanner = recentUserActivity.lastScannerUsed,
                )
            } ?: ProjectDetailsUiState(isLoaded = false)
        } catch (_: CancellationException) {
            // A real cancellation (the ViewModel is cleared) stops here; a stray one from a collaborator hides the card, as before.
            currentCoroutineContext().ensureActive()
            ProjectDetailsUiState(isLoaded = false)
        } catch (e: Exception) {
            Simber.e("Failed to load project details on dashboard", e)
            ProjectDetailsUiState(isLoaded = false)
        }
        reduce { state }
    }

    init {
        onAction(ProjectDetailsUiAction.Load)
    }
}
