package com.simprints.id.activities.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.infra.config.ConfigManager

class DashboardViewModelFactory(
    private val projectDetailsRepository: DashboardProjectDetailsRepository,
    private val syncCardStateRepository: DashboardSyncCardStateRepository,
    private val dailyActivityRepository: DashboardDailyActivityRepository,
    private val configManager: ConfigManager,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                projectDetailsRepository,
                syncCardStateRepository,
                dailyActivityRepository,
                configManager
            ) as T
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }

}
