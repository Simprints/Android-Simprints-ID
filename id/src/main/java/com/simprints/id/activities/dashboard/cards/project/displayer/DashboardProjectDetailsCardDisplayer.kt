package com.simprints.id.activities.dashboard.cards.project.displayer

import android.widget.LinearLayout
import com.simprints.id.activities.dashboard.cards.project.model.DashboardProjectWrapper

interface DashboardProjectDetailsCardDisplayer {
    fun initRoot(rootLayout: LinearLayout)
    fun displayProjectDetails(projectDetails: DashboardProjectWrapper)
}
