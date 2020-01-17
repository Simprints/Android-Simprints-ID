package com.simprints.id.activities.dashboard.cards.project

import android.widget.LinearLayout

interface DashboardProjectDetailsCardDisplayer {
    fun initRoot(rootLayout: LinearLayout)
    fun displayProjectDetails()
}
