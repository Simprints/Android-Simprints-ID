package com.simprints.id.activities.dashboard.cards.daily_activity.repository

import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.domain.moduleapi.app.responses.AppResponse

interface DashboardDailyActivityRepository {
    suspend fun getDailyActivity(): DashboardDailyActivityState
    suspend fun updateDailyActivity(appResponse: AppResponse)
}
