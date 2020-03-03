package com.simprints.id.activities.dashboard.cards.daily_activity.displayer

import android.view.ViewGroup
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState

interface DashboardDailyActivityCardDisplayer {
    fun initRoot(rootLayout: ViewGroup)
    fun displayDailyActivityState(dailyActivityState: DashboardDailyActivityState)
}
