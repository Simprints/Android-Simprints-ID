package com.simprints.id.activities.dashboard.cards.daily_activity.displayer

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simprints.id.R
import com.simprints.id.activities.dashboard.cards.daily_activity.model.DashboardDailyActivityState
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper

class DashboardDailyActivityCardDisplayerImpl(
    private val timeHelper: TimeHelper,
    private val androidResourcesHelper: AndroidResourcesHelper
) : DashboardDailyActivityCardDisplayer {

    private lateinit var root: ViewGroup

    override fun initRoot(rootLayout: ViewGroup) {
        root = rootLayout
    }

    override fun displayDailyActivityState(dailyActivityState: DashboardDailyActivityState) {
        with(root) {
            setTitle()
        }
    }

    private fun View.setTitle() {
        val date = timeHelper.getCurrentDateAsString()
        val text = androidResourcesHelper.getString(R.string.dashboard_card_activity, arrayOf(date))
        findViewById<TextView>(R.id.dashboard_daily_activity_card_title).text = text
    }

}
