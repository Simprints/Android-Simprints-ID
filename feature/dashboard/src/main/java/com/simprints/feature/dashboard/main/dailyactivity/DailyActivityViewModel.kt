package com.simprints.feature.dashboard.main.dailyactivity

import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.uibase.viewmodel.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class DailyActivityViewModel @Inject constructor(
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
) : BaseMviViewModel<DailyActivityUiState, DailyActivityUiAction, DailyActivityUiEffect>(DailyActivityUiState()) {
    override suspend fun handleAction(action: DailyActivityUiAction) {
        when (action) {
            DailyActivityUiAction.Load -> load()
        }
    }

    private suspend fun load() {
        val userActivity = recentUserActivityManager.getRecentUserActivity()
        val activity = DailyActivityUiState.DailyActivityUi(
            enrolments = userActivity.enrolmentsToday,
            identifications = userActivity.identificationsToday,
            verifications = userActivity.verificationsToday,
            currentDate = timeHelper.getCurrentDateAsString(),
        )
        reduce { it.copy(activity = activity) }
    }

    init {
        onAction(DailyActivityUiAction.Load)
    }
}
