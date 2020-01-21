package com.simprints.id.activities.dashboard.cards.project.repository

import androidx.lifecycle.LiveData
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper

interface DashboardProjectDetailsRepository {
    suspend fun getProjectDetails(): LiveData<DashboardProjectWrapper>
}
