package com.simprints.id.activities.dashboard.cards.project.displayer

import android.content.Context
import android.widget.LinearLayout
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectState

interface DashboardProjectDetailsCardDisplayer {
    fun initRoot(rootLayout: LinearLayout)
    fun displayProjectDetails(projectDetails: DashboardProjectState)
}
